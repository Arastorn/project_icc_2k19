package services.metadata.messages

import services.metadata.models._
import akka.actor._
import spray.json._

object MetadataMessages {
  case class MetadataResponse(status: JsValue)
  case class MetadataThrowServerError()
  case class PutImgMetadataToElasticSearchRequest(imgJson: JsValue)
}
