package services.elasticSearch.actors

import akka.actor.{Actor, ActorLogging, Props}
import spray.json._
import spray.json.DefaultJsonProtocol._

import services.elasticSearch.models._
import services.elasticSearch.messages.ElasticSearchMessages._

class ElasticSearchRequestHandler extends Actor with ActorLogging{

  def get(url: String) = scala.io.Source.fromURL(url).mkString

  override def receive: Receive = {

    case GetElasticSearchRequest =>
      println("Received GetElasticSearchRequest")
      sender() ! ElasticSearchResponse("test")
  }
}

object ElasticSearchRequestHandler {
  def props(): Props = {
    Props(classOf[ElasticSearchRequestHandler])
  }
}
