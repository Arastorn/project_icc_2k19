package services.tiles.messages

import services.tiles.models._
import akka.actor._
import spray.json._

object TilesMessages {
  case class TilesResponse(tiles: String)
  object GetTilesRequest
}
