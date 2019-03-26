package services.save.actors

import akka.actor.{Actor, ActorLogging, Props}
import spray.json._
import spray.json.DefaultJsonProtocol._

import services.save.models._
import services.save.messages.SaveMessages._

class SaveRequestHandler extends Actor with ActorLogging{

  def get(url: String) = scala.io.Source.fromURL(url).mkString

  override def receive: Receive = {

    case GetSaveRequest =>
      println("Received GetSaveRequest")
      sender() ! SaveResponse("test")
  }
}

object SaveRequestHandler {
  def props(): Props = {
    Props(classOf[SaveRequestHandler])
  }
}
