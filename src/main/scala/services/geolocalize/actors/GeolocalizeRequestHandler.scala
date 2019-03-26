package services.geolocalize.actors

import akka.actor.{Actor, ActorLogging, Props}
import spray.json._
import spray.json.DefaultJsonProtocol._

import services.geolocalize.models._
import services.geolocalize.messages.GeolocalizeMessages._

class GeolocalizeRequestHandler extends Actor with ActorLogging{

  def get(url: String) = scala.io.Source.fromURL(url).mkString

  override def receive: Receive = {

    case GetGeolocalizeRequest =>
      println("Received GetGeolocalizeRequest")
      sender() ! GeolocalizeResponse("test")
  }
}

object GeolocalizeRequestHandler {
  def props(): Props = {
    Props(classOf[GeolocalizeRequestHandler])
  }
}
