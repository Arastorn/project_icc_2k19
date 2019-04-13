package services.metadata.actors

import akka.actor.{Actor, ActorRef, ActorLogging, Props}
import spray.json._
import spray.json.DefaultJsonProtocol._
import sys.process._
import scalaj.http._
import scala.util.{Success, Failure}
import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.duration._
import scala.concurrent._
import java.io._
import java.nio.file.{Files, Path, Paths, StandardCopyOption}
import util.Try
import scala.xml.{XML, Elem}

import java.net.InetAddress
import java.net.ConnectException

import services.metadata.models._
import services.metadata.messages.MetadataMessages._

class MetadataRequestHandler extends Actor with ActorLogging{

  private def prepareData(json: JsValue): Map[String, String] = {
    json.convertTo[Map[String,String]]
  }

  private def fileExists(path: String): Boolean = {
    val file = new File(path)
    file.exists()
  }

  private def isDirectory(path: String): Boolean = {
    val file = new File(path)
    file.isDirectory()
  }

  private def isElasticUp(pathES: String): Boolean = {
    try {
      val response = Http(pathES).asString
      val expectedElasticsearch = "elasticsearch"
      response.body.parseJson.asJsObject.getFields("cluster_name").head.toString.dropRight(1).substring(1) == expectedElasticsearch
    } catch {
      case e: ConnectException => false
    }
  }

  private def readXML(pathXML: String) = {
    XML.loadFile(pathXML)
  }

  private def putToElasticSearch(imgName: String, elasticRoute: String, jsonMetadata: JsValue): JsValue = {
    val request = Http(s"${elasticRoute}/images/metadata/${imgName}").header("content-type", "application/json").postData(s"""${jsonMetadata.toString}""")
    request.asString.body.parseJson.toJson
  }

  private def getMetadata(xml: Elem): JsValue = {
    val firstChildNode = xml.collect{
      case el: Elem => el.label
    }.headOption.getOrElse("").toString
    val qualityIndicatorsInfoMetadataMap = (xml \\ firstChildNode \\ "Quality_Indicators_Info" \\ "Image_Content_QI" \ "_").toList.map(e => e.collect{
      case el: Elem => el.label
    }.headOption.getOrElse("").toString).map(
      e => (e, (xml \\ firstChildNode \\ "Quality_Indicators_Info" \\ "Image_Content_QI" \\ e).text.toDouble)
    ).toMap
    Metadata(
      GeneralInfoMetadata(
        (xml \\ firstChildNode \\ "General_Info" \\ "TILE_ID").text.toString,
        (xml \\ firstChildNode \\ "General_Info" \\ "DATASTRIP_ID").text.toString,
        (xml \\ firstChildNode \\ "General_Info" \\ "SENSING_TIME").text.toString,
        ArchivingInfoMetadata(
          (xml \\ firstChildNode \\ "General_Info" \\ "Archiving_Info" \\ "ARCHIVING_CENTRE").text.toString,
          (xml \\ firstChildNode \\ "General_Info" \\ "Archiving_Info" \\ "ARCHIVING_TIME").text.toString
        )
      ),
      GeometricInfoMetadata(
        (xml \\ firstChildNode \\ "Geometric_Info" \\ "Tile_Geocoding" \\ "HORIZONTAL_CS_NAME").text.toString,
        (xml \\ firstChildNode \\ "Geometric_Info" \\ "Tile_Geocoding" \\ "HORIZONTAL_CS_CODE").text.toString
      ),
      QualityIndicatorsInfoMetadata(
        qualityIndicatorsInfoMetadataMap
      )
    ).toJson
  }

  private def elasticAdditionSuccess(body: JsValue): Boolean = {
    val resValue = body.asJsObject.getFields("result").head.toString.dropRight(1).substring(1)
    resValue == "updated" || resValue == "created"
  }

  def postElasticMetadata(imgJson: JsValue, father: ActorRef): Unit = {
    val ipHostname = InetAddress.getLocalHost().toString.split("/")
    val hostname = ipHostname(0)
    val ip = ipHostname(1)
    var elasticRoute = ""
    if ( hostname == "osboxes" ) {
       elasticRoute = "http://" + ip + ":9200"
    } else {
       elasticRoute = "http://localhost:9200"
    }        
    val name = prepareData(imgJson)("name")
    val pathToTheImage = "images/" + name
    val pathToTheMetadata = pathToTheImage + "/MTD_TL.xml"
    if (isElasticUp(elasticRoute)) {
      if (fileExists(pathToTheImage) && isDirectory(pathToTheImage)) {
        if (fileExists(pathToTheMetadata)) {
          val metadata = getMetadata(readXML(pathToTheMetadata))
          val body = putToElasticSearch(name,elasticRoute,metadata)
          if (elasticAdditionSuccess(body)) {
            father ! MetadataResponse(PostMetadataRequestStatus(name, s"Metadata for image ${name} has been reached, it has been added to elasticSearch at route ${elasticRoute}", "SUCCESS", body, metadata).toJson)
          } else {
            father ! MetadataResponse(PostMetadataRequestErrorElasticStatus(name, s"Metadata for image ${name} has been reached, but addition to elastic failed at ${elasticRoute}", "SUCCESS", body).toJson)
          }
        } else {
          father ! MetadataResponse(PostMetadataRequestErrorStatus(name, s"${pathToTheMetadata} is not known, try to download a correct image before using this service", "IMAGE_WITHOUT_METADATA").toJson)
        }
      } else {
        father ! MetadataResponse(PostMetadataRequestErrorStatus(name, s"${pathToTheImage} is not known, try to download a correct image before using this service", "IMAGE_NOT_REFERENCED").toJson)
      }
    } else {
      father ! MetadataResponse(PostMetadataRequestErrorStatus(name, "Elastic search service is down, try again later ...", "ELASTIC_SEARCH_NOT_REACHABLE").toJson)
    }

  }

  override def receive: Receive = {

    case PutImgMetadataToElasticSearchRequest(imgJson) =>
      println("Received GetMetadataRequest")
      postElasticMetadata(imgJson, sender())
  }

}

object MetadataRequestHandler {
  def props(): Props = {
    Props(classOf[MetadataRequestHandler])
  }
}
