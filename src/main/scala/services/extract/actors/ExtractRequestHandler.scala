package services.extract.actors

import akka.actor.{Actor, ActorLogging, Props}
import spray.json._
import spray.json.DefaultJsonProtocol._

import services.extract.models._
import services.extract.messages.ExtractMessages._

class ExtractRequestHandler extends Actor with ActorLogging {

  def requestDBPEDIA(text: JsValue) = {
    val dataMap = text.convertTo[Map[String,String]]
    if (dataMap.keySet.exists(data => data == "description")) {
      val data = dataMap("description")
      println(data)
    }
  }

  override def receive: Receive = {
    case ExtractTextInHeaderRequest(text) =>
      requestDBPEDIA(text)
      sender() ! ExtractResponse("Ok")
  }

}

object ExtractRequestHandler {
  def props(): Props = {
    Props(classOf[ExtractRequestHandler])
  }
}
