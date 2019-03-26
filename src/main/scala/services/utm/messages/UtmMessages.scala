package services.utm.messages

import services.utm.models._
import akka.actor._
import spray.json._

object UtmMessages {
  case class UtmResponse(utm: String)
  object GetUtmRequest
}
