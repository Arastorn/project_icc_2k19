package services.tiles.models

import spray.json._
import DefaultJsonProtocol._

final case class CorrectTiles(
  imgName: String,
  tilesPath: String,
  status: String
)

final case class ErrorTiles(
  path: String,
  error: String
)

final case class ComputeStatus(
  imgName: String,
  status: String,
  statusCode: String
)

object CorrectTiles extends DefaultJsonProtocol {
  implicit val productFormat = jsonFormat3(CorrectTiles.apply)
}

object ErrorTiles extends DefaultJsonProtocol {
  implicit val productFormat = jsonFormat2(ErrorTiles.apply)
}

object ComputeStatus extends DefaultJsonProtocol {
  implicit val productFormat = jsonFormat3(ComputeStatus.apply)
}
