package services.utm.actors

import akka.actor.{Actor, ActorLogging, Props}
import spray.json._
import spray.json.DefaultJsonProtocol._
import scalaj.http.Http

import services.utm.models._
import services.utm.messages.UtmMessages._

class UtmRequestHandler extends Actor with ActorLogging{

  private def parseJSONResources(json: JsValue): JsValue = {
    Map(
      "utm_coordinates" -> json
    ).toJson
  }

  private def prepareData(text: JsValue) = {
    val dataMap = text.convertTo[Map[String,List[Map[String,String]]]]
    val ApiPublicKey = "27277e6d05d542e297fab043fbf02538"
    dataMap("coords").map(
      item => Http("https://api.opencagedata.com/geocode/v1/json?q="+item("latitude")+"+"+item("longitude")+"&key="+ApiPublicKey).asString.body
    ).map(
      item => item.parseJson.asJsObject.getFields("results").head.toString.dropRight(1).substring(1)
                  .parseJson.asJsObject.getFields("annotations").head.toString
                  .parseJson.asJsObject.getFields("Mercator").head.toString
                  .parseJson.convertTo[Utm]
    ).toJson
  }

  private def prepareDataMGRS(text: JsValue) = {
    val dataMap = text.convertTo[Map[String,List[Map[String,String]]]]
    val ApiPublicKey = "27277e6d05d542e297fab043fbf02538"
    dataMap("coords").map(
      item => Http("https://api.opencagedata.com/geocode/v1/json?q="+item("latitude")+"+"+item("longitude")+"&key="+ApiPublicKey).asString.body
    ).map(
      item => item.parseJson.asJsObject.getFields("results").head.toString.dropRight(1).substring(1)
                  .parseJson.asJsObject.getFields("annotations").head.toString
                  .parseJson.asJsObject.getFields("MGRS").head.toString
                  .parseJson
    ).toJson
  }

  override def receive: Receive = {

    case UtmGetRequest(text) =>
      val data = prepareData(text)
      val json = parseJSONResources(data)
      sender() ! UtmResponse(json)
    case UtmGetRequestMGRS(text) =>
      val data = prepareDataMGRS(text)
      val json = parseJSONResources(data)
      sender() ! UtmResponse(json)
  }
}

object UtmRequestHandler {
  def props(): Props = {
    Props(classOf[UtmRequestHandler])
  }
}
