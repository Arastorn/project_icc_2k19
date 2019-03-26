package services.utm.actors

import akka.actor.{Actor, ActorLogging, Props}
import spray.json._
import spray.json.DefaultJsonProtocol._

import services.utm.models._
import services.utm.messages.UtmMessages._

class UtmRequestHandler extends Actor with ActorLogging{

  def get(url: String) = scala.io.Source.fromURL(url).mkString

  override def receive: Receive = {

    case GetUtmRequest =>
      println("Received GetUtmRequest")
      sender() ! UtmResponse("test")
  }
}

object UtmRequestHandler {
  def props(): Props = {
    Props(classOf[UtmRequestHandler])
  }
}
