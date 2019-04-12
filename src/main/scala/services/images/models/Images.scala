package services.images.models

import spray.json._
import DefaultJsonProtocol._

final case class WrongCoords(
  status: String,
  statusCode: String
)

final case class ImageFound(
  status: String,
  img_downloading: List[String],
  img_waiting: List[String],
  img_downloaded: List[String]
)

object WrongCoords extends DefaultJsonProtocol {
  implicit val productFormat = jsonFormat2(WrongCoords.apply)
}

object ImageFound extends DefaultJsonProtocol {
  implicit val productFormat = jsonFormat4(ImageFound.apply)
}
