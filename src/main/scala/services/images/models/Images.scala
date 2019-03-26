package services.images.models

import spray.json._
import DefaultJsonProtocol._

final case class Images(
  namme: String,
  texte: String
)

object Images extends DefaultJsonProtocol {
  implicit val productFormat = jsonFormat2(Images.apply)
}
