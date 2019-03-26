package services.hiking.actors

import akka.actor.{Actor, ActorLogging, Props}
import spray.json._
import spray.json.DefaultJsonProtocol._

import services.hiking.models._
import services.hiking.messages.HikingMessages._

class HikingRequestHandler extends Actor with ActorLogging{

  def get(url: String) = scala.io.Source.fromURL(url).mkString

  override def receive: Receive = {

    case GetHikingRequest =>
      println("Received GetHikingRequest")
      sender() ! HikingResponse("test")
  }
}

object HikingRequestHandler {
  def props(): Props = {
    Props(classOf[HikingRequestHandler])
  }
}
