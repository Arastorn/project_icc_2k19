package services.save.messages

import services.save.models._
import akka.actor._
import spray.json._

object SaveMessages {
  case class WrongJson(json: JsValue)
  case class SaveUploading(json: JsValue)
  case class SaveUploaded(json: JsValue)
  case class GetSaveRequest(imageName: JsValue)
}
