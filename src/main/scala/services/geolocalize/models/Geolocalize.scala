package services.geolocalize.models

import spray.json._
import DefaultJsonProtocol._

final case class Geolocalize(
  namme: String,
  texte: String
)

object Geolocalize extends DefaultJsonProtocol {
  implicit val productFormat = jsonFormat2(Geolocalize.apply)
}
