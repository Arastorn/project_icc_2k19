package services.hiking.messages

import services.hiking.models._
import akka.actor._
import spray.json._

object HikingMessages {
  case class GetHikingById(id: Int)
  case class HikingResponse(hiking: Hiking)
  case class HikingThrowServerError()
  object GetHikingRequest
}
