package services.hiking.actors

//package
import akka.actor.{Actor, ActorLogging, Props}
import spray.json._
import spray.json.DefaultJsonProtocol._
import scalaj.http._

// services
import services.hiking.models._
import services.hiking.messages.HikingMessages._

class HikingRequestHandler extends Actor with ActorLogging{

  private def parseNameJson(json: JsValue): String =
    json.asJsObject.getFields("name") match {
      case Seq(JsString(name)) =>
        name
      case _ => throw new DeserializationException("name: String expected")
    }

  private def parseDescriptionJson(json: JsValue): String =
    json.asJsObject.getFields("description") match {
      case Seq(JsString(description)) =>
        description.replaceAll("\n","")
      case _ => throw new DeserializationException("description: String expected")
    }

  private def parseChoucasApiToHiking(text: String): JsValue = {
      text.dropRight(1).substring(1).parseJson
  }

  def requestAndParseHikingById(jsonText: String) : Hiking = {
    val hikingParsed = parseChoucasApiToHiking(jsonText)
    Hiking(parseNameJson(hikingParsed),parseDescriptionJson(hikingParsed))
  }

  override def receive: Receive = {

    case GetHikingById(id) =>
      println("GetHikingById")
      val jsonAsText = Http("https://choucas.blqn.fr/data/outing/" + id.toString).asString.body
      if(jsonAsText.length() < 3) {
        sender() ! HikingThrowServerError()
      } else {
        sender() ! HikingResponse(requestAndParseHikingById(jsonAsText))
      }
  }
}

object HikingRequestHandler {
  def props(): Props = {
    Props(classOf[HikingRequestHandler])
  }
}
