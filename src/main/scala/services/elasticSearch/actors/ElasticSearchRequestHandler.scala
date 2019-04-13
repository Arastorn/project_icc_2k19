package services.elasticSearch.actors

import akka.actor.{Actor, ActorRef, ActorLogging, Props}
import spray.json._
import spray.json.DefaultJsonProtocol._
import scalaj.http._

import java.net.InetAddress
import java.net.ConnectException

import services.elasticSearch.models._
import services.elasticSearch.messages.ElasticSearchMessages._

class ElasticSearchRequestHandler extends Actor with ActorLogging{

  //(longitude, latitude)
  private def prepareData(json: JsValue): Map[String,Coordinates] = {
    val coords = parseBoundingBox(json)
    val ne = getCoordsFromNe(coords)
    val sw = getCoordsFromSw(coords)
    Map("ne" -> Coordinates(ne._2,ne._1), "sw" -> Coordinates(sw._2,sw._1))
  }

  private def getItemFromJsKey(json: JsValue, key: String): JsValue = {
    json.asJsObject.getFields(key) match {
      case Seq(item) =>
        item
      case _ => "{}".parseJson
    }
  }

  private def parseBoundingBox(jsonCoords: JsValue): JsValue = {
    getItemFromJsKey(jsonCoords,"boundingbox")
  }

  private def getCoordsFromNe(jsonCoords: JsValue): (String,String) = {
    jsonCoords.asJsObject.getFields("ne") match {
      case Seq(ne) =>
        (ne.asJsObject.getFields("lng")(0).toString,ne.asJsObject.getFields("lat")(0).toString)
      case _ => ("","")
    }
  }

  private def getCoordsFromSw(jsonCoords: JsValue): (String,String) = {
    jsonCoords.asJsObject.getFields("sw") match {
      case Seq(sw) =>
        (sw.asJsObject.getFields("lng")(0).toString,sw.asJsObject.getFields("lat")(0).toString)
      case _ => ("","")
    }
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

  private def putToElasticSearch(imgName: String, elasticRoute: String, jsonMetadata: JsValue): JsValue = {
    val request = Http(s"${elasticRoute}/images/metadata/${imgName}").header("content-type", "application/json").postData(s"""${jsonMetadata.toString}""")
    request.asString.body.parseJson.toJson
  }

  private def researchES(elasticRoute: String, data: Map[String, Coordinates]): JsValue = {
    val query = """{"query":{"term":{"_id":"036febe9-a5c8-5b0e-83e5-c8a565a1eea5"}}}"""
    val request = Http(s"${elasticRoute}/images/metadata/_search").header("content-type", "application/json").postData(query)
    request.asString.body.parseJson.toJson
  }

  private def queryElasticSearch(boundingBox: JsValue, father: ActorRef): Unit = {
    val ipHostname = InetAddress.getLocalHost().toString.split("/")
    val hostname = ipHostname(0)
    val ip = ipHostname(1)
    var elasticRoute = ""
    if ( hostname == "osboxes" ) {
       elasticRoute = "http://" + ip + ":9200"
    } else {
       elasticRoute = "http://localhost:9200"
    }
    if (isElasticUp(elasticRoute)) {
      val data = prepareData(boundingBox)
      val res = researchES(elasticRoute, data)
      father ! ElasticSearchQueryResponse(res)
    } else {
      father ! ElasticSearchQueryResponse(ElasticSearchErrorStatus(elasticRoute, "Elastic search service is down, try again later ...", "ELASTIC_SEARCH_NOT_REACHABLE").toJson)
    }
  }

  override def receive: Receive = {

    case BoundingBoxQueryElasticSearch(boundingBox) =>
      println("Received GetElasticSearchRequest")
      queryElasticSearch(boundingBox,sender())
    case _ =>
      sender() ! ElasticSearchQueryThrowServerError()
  }
}

object ElasticSearchRequestHandler {
  def props(): Props = {
    Props(classOf[ElasticSearchRequestHandler])
  }
}
