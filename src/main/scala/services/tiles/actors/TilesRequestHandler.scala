package services.tiles.actors

import akka.actor.{Actor, ActorLogging, Props}
import spray.json._
import spray.json.DefaultJsonProtocol._

import services.tiles.models._
import services.tiles.messages.TilesMessages._

class TilesRequestHandler extends Actor with ActorLogging{

  def get(url: String) = scala.io.Source.fromURL(url).mkString

  override def receive: Receive = {

    case GetTilesRequest =>
      println("Received GetTilesRequest")
      sender() ! TilesResponse("test")
  }
}

object TilesRequestHandler {
  def props(): Props = {
    Props(classOf[TilesRequestHandler])
  }
}
