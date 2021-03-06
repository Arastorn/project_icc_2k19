package services.extract.actors

import akka.actor.{Actor, ActorLogging, Props}
import spray.json._
import spray.json.DefaultJsonProtocol._
import scalaj.http.Http
import sys.process._

import services.extract.models._
import services.extract.messages.ExtractMessages._

class ExtractRequestHandler extends Actor with ActorLogging {

  private def parseJSONResources(json: JsValue): JsValue = {
    Map("entities" ->
      json.asJsObject.getFields("Resources").head.toString.dropRight(1).substring(1).replace("{","").replace("}","").split(",").toList.map(item => item.toString).filter(
        item => item.contains("@URI")
      ).map(
        item => item.substring(7).replace("\"","") //"@URI:"
      ).toList
    ).toJson
  }

  private def prepareData(text: JsValue): String = {
    val dataMap = text.convertTo[Map[String,String]]
    if (dataMap.keySet.exists(data => data == "description")) {
      var dataName = ""
      if (dataMap.keySet.exists(data => data == "name")) {
        dataName = dataMap("name")
      }
      val dataDesc = dataMap("description")
      dataName.concat(" ").concat(dataDesc)
    } else {
      "{}"
    }
  }

  private def requestDBPEDIA(text: JsValue) = {
      val data = prepareData(text)
      if (data != "") {
        val request = Http("http://icc.pau.eisti.fr/rest/annotate")
        val response = request.header("Accept", "application/json").postForm(Seq("text" -> data, "confidence" -> "0.2", "support" -> "50")).asString
        parseJSONResources(response.body.parseJson)
      } else {
        data.toJson
      }
  }

  override def receive: Receive = {
    case ExtractTextInHeaderRequestPost(text) =>
      val res = requestDBPEDIA(text)
      if (res.toString.length() < 5) {
        sender() ! ExtractResponseNotFound()
      } else {
        sender() ! ExtractResponse(res)
      }
    case _ =>
      sender() ! ExtractResponseNotFound()
  }

}

object ExtractRequestHandler {
  def props(): Props = {
    Props(classOf[ExtractRequestHandler])
  }
}
