package services.tiles.models

import spray.json._
import DefaultJsonProtocol._

final case class CorrectTiles(
  path: String,
  status: String
)

final case class ErrorTiles(
  path: String,
  error: String
)

object CorrectTiles extends DefaultJsonProtocol {
  implicit val productFormat = jsonFormat2(CorrectTiles.apply)
}

object ErrorTiles extends DefaultJsonProtocol {
  implicit val productFormat = jsonFormat2(ErrorTiles.apply)
}
