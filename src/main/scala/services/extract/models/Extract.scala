package services.extract.models

import spray.json._
import DefaultJsonProtocol._
import scala.collection.Map

final case class Extract(
  text: String
)

object Extract extends DefaultJsonProtocol {
  implicit val extractFormat = jsonFormat1(Extract.apply)
}
