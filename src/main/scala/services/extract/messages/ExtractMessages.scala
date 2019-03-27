package services.extract.messages

import services.products.models._
import akka.actor._
import spray.json._

object ExtractMessages {

  case class ExtractResponse(text: JsValue)
  case class ExtractTextInHeaderRequestPost(text: JsValue)
  case class ExtractThrowServerError()
  case class ExtractResponseNotFound()

  object GetExtractRequest

}
