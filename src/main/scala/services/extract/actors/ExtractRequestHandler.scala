package services.extract.actors

import akka.actor.{Actor, ActorLogging, Props}
import spray.json._
import spray.json.DefaultJsonProtocol._
import scalaj.http.Http
import sys.process._

import services.extract.models._
import services.extract.messages.ExtractMessages._

class ExtractRequestHandler extends Actor with ActorLogging {

  private def parseJSONResources(json: JsValue) = {
    json.asJsObject.getFields("Resources")(0).toString.dropRight(1).substring(1).split("}, {").map(item => item.replaceAll("{",""))
  }

  private def requestDBPEDIA(text: JsValue, confidence: Double): JsValue = {
    val dataMap = text.convertTo[Map[String,String]]
    if (dataMap.keySet.exists(data => data == "description")) {
      val data = dataMap("description")
      val request = Http("http://icc.pau.eisti.fr/rest/annotate")
      val response = request.header("Accept", "application/json").postForm(Seq("text" -> data, "confidence" -> confidence.toString, "support" -> "50")).asString
      val responseMap = response.body.parseJson.prettyPrint
      println(responseMap)
      //println(parseJSONResources(responseMap))
      return """ {"value":"success"} """.parseJson
    } else {
      return """ {"value":"error"} """.parseJson
    }
  }

  override def receive: Receive = {
    case ExtractTextInHeaderRequestPost(text) =>
      val res = requestDBPEDIA(text,0.1)
      sender() ! ExtractResponse(res)
  }

}

object ExtractRequestHandler {
  def props(): Props = {
    Props(classOf[ExtractRequestHandler])
  }
}
