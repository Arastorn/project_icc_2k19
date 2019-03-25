package services.hiking.actors

import akka.actor.{Actor, ActorLogging, Props}
import spray.json._
import spray.json.DefaultJsonProtocol._

import services.hiking.models._
import services.hiking.messages.HikingMessages._

class HikingRequestHandler extends Actor with ActorLogging{


  override def receive: Receive = {

    case GetHikingRequest =>
      println("Received GetHikingRequest")
      
  }
}

object HikingRequestHandler {
  def props(): Props = {
    Props(classOf[HikingRequestHandler])
  }
}
