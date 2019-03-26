package services.save.models

import spray.json._
import DefaultJsonProtocol._

final case class Save(
  namme: String,
  texte: String
)

object Save extends DefaultJsonProtocol {
  implicit val productFormat = jsonFormat2(Save.apply)
}
