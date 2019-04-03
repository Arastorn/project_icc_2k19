package services.images.actors

import akka.actor.{Actor, ActorLogging, Props}
import spray.json._
import spray.json.DefaultJsonProtocol._
import scalaj.http._
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.GregorianCalendar;

import services.images.models._
import services.images.messages.ImagesMessages._

class ImagesRequestHandler extends Actor with ActorLogging{

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

  def parseBoundingBox(jsonCoords: JsValue): JsValue = {
    jsonCoords.asJsObject.getFields("boundingbox") match {
      case Seq(boundingbox) =>
        boundingbox
      case _ => "{}".parseJson
    }
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
      case Seq(features) =>
        features
      case _ => "{}".parseJson
    }
  }

  def getFirstItem(jsonArray: JsValue) = {
    val toRemove = "[]".toSet
    jsonArray.toString.filterNot(toRemove)
  }

  override def receive: Receive = {

    case request: GetImagesRequest =>
      println("Received GetImagesRequest")
      val urlImages = getImagesUrl(request.coords)
      println(getFirstItem(getMostRecentImage(urlImages)))
      sender() ! ImagesResponse(getMostRecentImage(urlImages))
  }
}

object ImagesRequestHandler {
  def props(): Props = {
    Props(classOf[ImagesRequestHandler])
  }
}
