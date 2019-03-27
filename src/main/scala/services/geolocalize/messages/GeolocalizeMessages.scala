package services.geolocalize.messages

import services.geolocalize.models._
import akka.actor._
import spray.json._

object GeolocalizeMessages {
  case class GeolocalizeResponse(geolocalize: JsValue)
  case class GeolocalizeThrowServerNotFound()
  case class GeolocalizeResponseNotFound(json: JsValue)
  case class GetGeolocalizeRequest(ensReport: JsValue)
}
