package services.metadata.messages

import services.metadata.models._
import akka.actor._
import spray.json._

object MetadataMessages {
  case class MetadataResponse(metadata: String)
  object GetMetadataRequest
}
