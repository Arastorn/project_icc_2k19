package services.tiles.models

import spray.json._
import DefaultJsonProtocol._

final case class Tiles(
  namme: String,
  texte: String
)

object Tiles extends DefaultJsonProtocol {
  implicit val productFormat = jsonFormat2(Tiles.apply)
}
