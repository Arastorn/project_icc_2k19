package services.metadata.actors

import akka.actor.{Actor, ActorLogging, Props}
import spray.json._
import spray.json.DefaultJsonProtocol._

import services.metadata.models._
import services.metadata.messages.MetadataMessages._

class MetadataRequestHandler extends Actor with ActorLogging{

  def get(url: String) = scala.io.Source.fromURL(url).mkString

  override def receive: Receive = {

    case GetMetadataRequest =>
      println("Received GetMetadataRequest")
      sender() ! MetadataResponse("test")
  }
}

object MetadataRequestHandler {
  def props(): Props = {
    Props(classOf[MetadataRequestHandler])
  }
}
