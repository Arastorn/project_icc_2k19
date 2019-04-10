package services.save.models

import spray.json._
import DefaultJsonProtocol._

final case class CorrectSave(
  status: String,
  statusCode: String
)

final case class AlreadySaved(
  status: String,
  url: String,
  statusCode: String
)

final case class WrongSave(
  status: String,
  statusCode: String
)

object CorrectSave extends DefaultJsonProtocol {
  implicit val productFormat = jsonFormat2(CorrectSave.apply)
}


object AlreadySaved extends DefaultJsonProtocol {
  implicit val productFormat = jsonFormat3(AlreadySaved.apply)
}

object WrongSave extends DefaultJsonProtocol {
  implicit val productFormat = jsonFormat2(WrongSave.apply)
}
