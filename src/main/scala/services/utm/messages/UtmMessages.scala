package services.utm.messages

import services.utm.models._
import akka.actor._
import spray.json._

object UtmMessages {

  case class UtmResponse(utm: JsValue)
  case class UtmGetRequest(text: JsValue)
  case class UtmResponseNotFound()

  object GetUtmRequest
}
