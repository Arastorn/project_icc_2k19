package services.utm.models

import spray.json._
import DefaultJsonProtocol._

final case class Utm(
  namme: String,
  texte: String
)

object Utm extends DefaultJsonProtocol {
  implicit val productFormat = jsonFormat2(Utm.apply)
}
