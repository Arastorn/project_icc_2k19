package services.hiking.messages

import services.hiking.models._
import akka.actor._
import spray.json._

object HikingMessages {
  case class HikingResponse(hiking: String)
  object GetHikingRequest
}
