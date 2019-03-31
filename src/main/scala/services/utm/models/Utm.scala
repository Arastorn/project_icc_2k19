package services.utm.models

import spray.json._
import DefaultJsonProtocol._

final case class Utm(
  sw: Map[String,Double],
  ne: Map[String,Double]
)

object Utm extends DefaultJsonProtocol {
  implicit val productFormat = jsonFormat2(Utm.apply)
}
