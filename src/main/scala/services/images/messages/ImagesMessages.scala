package services.images.messages

import services.images.models._
import akka.actor._
import spray.json._

object ImagesMessages {
  case class ImagesResponse(images: JsValue)
  case class GetImagesRequest(coords: JsValue)
}
