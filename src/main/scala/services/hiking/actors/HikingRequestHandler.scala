package services.hiking.actors

//package
import akka.actor.{Actor, ActorLogging, Props}
import spray.json._
import spray.json.DefaultJsonProtocol._
import scala.util.parsing.json._
import scalaj.http._

// services
import services.hiking.models._
import services.hiking.messages.HikingMessages._

class HikingRequestHandler extends Actor with ActorLogging{

  private def parseNameJson(json: JsValue): String =
    json.asJsObject.getFields("name") match {
      case Seq(JsString(name)) =>
        name
      case _ =>
      throw new DeserializationException("label: String expected")
    }

  def requestAndParseHiking = {
    val request: HttpRequest = Http("https://choucas.blqn.fr/data/outing/921410")
    val json = request.asString.body.toJson
    println(parseNameJson(json))
  }

  override def receive: Receive = {

    case GetHikingRequest =>
      println("Received GetHikingRequest")
      requestAndParseHiking
      sender() ! HikingResponse("ok")
  }
}

object HikingRequestHandler {
  def props(): Props = {
    Props(classOf[HikingRequestHandler])
  }
}
