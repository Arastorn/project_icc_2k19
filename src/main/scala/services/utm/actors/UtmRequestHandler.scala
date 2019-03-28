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
      "boundingbox" -> json
    ).toJson
  }

  private def prepareData(text: JsValue) = {
    val dataMap = text.convertTo[Map[String,List[Map[String,String]]]]
    val (latitudes, longitudes) = dataMap("coords").map(
      item => (item("latitude").toDouble,item("longitude").toDouble)
    ).unzip
    Utm(sw = Map("lng" -> longitudes.min, "lat" -> latitudes.min), ne = Map("lng" -> longitudes.max, "lat" -> latitudes.max)).toJson
  }

  override def receive: Receive = {

    case UtmGetRequest(text) =>
      val data = prepareData(text)
      val json = parseJSONResources(data)
      sender() ! UtmResponse(json)
  }
}

object UtmRequestHandler {
  def props(): Props = {
    Props(classOf[UtmRequestHandler])
  }
}
