package services.images.actors

import akka.actor.{Actor, ActorLogging, Props}
import akka.actor.ActorSystem
import akka.stream.ActorMaterializer
import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.duration._
import scala.concurrent._
import spray.json._
import spray.json.DefaultJsonProtocol._
import scalaj.http._
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.GregorianCalendar;
import java.io._
import sys.process._


import services.images.models._
import services.images.messages.ImagesMessages._

class ImagesRequestHandler extends Actor with ActorLogging{

  //implicit val system = ActorSystem()
  //implicit val executionContext = system.dispatcher

  def getCoordsFromSw(jsonCoords: JsValue): (String,String) = {
    jsonCoords.asJsObject.getFields("sw") match {
      case Seq(sw) =>
        (sw.asJsObject.getFields("lng")(0).toString,sw.asJsObject.getFields("lat")(0).toString)
      case _ => ("","")
    }
  }

  def getCoordsFromNe(jsonCoords: JsValue): (String,String) = {
    jsonCoords.asJsObject.getFields("ne") match {
      case Seq(ne) =>
        (ne.asJsObject.getFields("lng")(0).toString,ne.asJsObject.getFields("lat")(0).toString)
      case _ => ("","")
    }
  }

  def getItemFromJsKey(json: JsValue, key: String): JsValue = {
    json.asJsObject.getFields(key) match {
      case Seq(item) =>
        item
      case _ => "{}".parseJson
    }
  }

  def parseBoundingBox(jsonCoords: JsValue): JsValue = {
    getItemFromJsKey(jsonCoords,"boundingbox")
  }

  def getDate(): String = {
    val format = new SimpleDateFormat("yyyy-MM-dd")
    val date = Calendar.getInstance()
    date.add(Calendar.DATE, -2)
    format.format(date.getTime())
  }

  def sendRequestOnPeps(ne:(String,String), sw:(String,String)): JsValue = {
    Http("https://peps.cnes.fr/resto/api/collections/S2ST/search.json").param("box", ne._1 + "," + ne._2 + "," + sw._1 + "," + sw._2).param("startDate",getDate()).header("Accept", "application/json").asString.body.parseJson
  }

  def getImagesUrl(jsonCoords: JsValue): JsValue = {
    val coords = parseBoundingBox(jsonCoords)
    val ne = getCoordsFromNe(coords)
    val sw = getCoordsFromSw(coords)
    sendRequestOnPeps(ne,sw)
  }

  def getMostRecentImage(jsonFromPeps: JsValue): JsValue = {
    jsonFromPeps.asJsObject.getFields("features") match {
      case Seq(JsArray(features)) =>
        features(0)
      case _ => "{}".parseJson
    }
  }


  def getProperties(jsonImage: JsValue): JsValue = {
    getItemFromJsKey(jsonImage,"properties")
  }

  def getService(jsonProperties: JsValue): JsValue = {
    getItemFromJsKey(jsonProperties, "services")
  }

  def getDownload(jsonService: JsValue): JsValue = {
    getItemFromJsKey(jsonService,"download")
  }

  def getUrl(jsonDownload: JsValue): String = {
    jsonDownload.asJsObject.getFields("url") match {
      case Seq(JsString(url)) =>
        url
      case _ => ""
    }
  }

  def createScript(url: String) = {
    val script = new PrintWriter(new File("download/script.sh" ))
    val requete = "wget --quiet --method GET --header 'Authorization: Basic Ym91cmdlb2lzYUBlaXN0aS5ldTpBZHJpZW42Ng==' --header 'cache-control: no-cache' --output-document - " + url + " >> download/images.zip"
    script.write(requete)
    script.close
  }

  def launchScript(): Future[Unit] = Future {
    val script = "./download/script.sh" !!
  }


  def getUrlFromFirstImage(jsonArray: JsValue) = {
    val properties = getProperties(jsonArray)
    val service = getService(properties)
    val download = getDownload(service)
    getUrl(download)
  }

  override def receive: Receive = {

    case request: GetImagesRequest =>
      println("Received GetImagesRequest")
      val url = getUrlFromFirstImage(getMostRecentImage(getImagesUrl(request.coords)))
      if(url.length() > 15) {
        createScript(url)
        launchScript()
        sender() ! ImagesResponse("{\"status\": \"Images Found ! Wait for the dowload in the download/images.zip folder\"}".parseJson)
      }

  }
}

object ImagesRequestHandler {
  def props(): Props = {
    Props(classOf[ImagesRequestHandler])
  }
}
