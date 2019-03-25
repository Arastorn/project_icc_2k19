package services.hiking.models

import spray.json._
import DefaultJsonProtocol._

final case class Hiking(
  texte: String
)

object Hiking extends DefaultJsonProtocol {
  implicit val productFormat = jsonFormat1(Hiking.apply)
}
