package services.save.messages

import services.save.models._
import akka.actor._
import spray.json._

object SaveMessages {
  case class SaveResponse(save: JsValue)
  case class GetSaveRequest(imageName: JsValue)
}
