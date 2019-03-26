package services.metadata.models

import spray.json._
import DefaultJsonProtocol._

final case class Metadata(
  namme: String,
  texte: String
)

object Metadata extends DefaultJsonProtocol {
  implicit val productFormat = jsonFormat2(Metadata.apply)
}
