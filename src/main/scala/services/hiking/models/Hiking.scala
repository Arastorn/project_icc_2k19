package services.hiking.models

import spray.json._
import DefaultJsonProtocol._

final case class Hiking(
  name: String,
  description: String
)

object Hiking extends DefaultJsonProtocol {
  implicit val productFormat = jsonFormat2(Hiking.apply)
}
