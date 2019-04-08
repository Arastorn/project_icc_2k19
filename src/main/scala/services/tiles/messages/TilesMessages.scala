package services.tiles.messages

import services.tiles.models._
import akka.actor._
import spray.json._

object TilesMessages {
  case class TilesResponse(pathTiles: JsValue)
  case class TilesThrowServerError()
  case class GetTilesRequest(pathImage: JsValue)
}
