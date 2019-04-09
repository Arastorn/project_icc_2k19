package services.save.actors

import akka.actor.{Actor, ActorLogging, Props}
import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.duration._
import scala.concurrent._
import akka.actor.ActorSystem
import akka.stream.ActorMaterializer
import spray.json._
import spray.json.DefaultJsonProtocol._
import sys.process._


import services.save.models._
import services.save.messages.SaveMessages._

class SaveRequestHandler extends Actor with ActorLogging{

  def getImageName(json: JsValue): String = {
    json.asJsObject.getFields("image") match {
      case Seq(JsString(image)) =>
        image
      case _ => ""
    }
  }

  def getUserName(json: JsValue): String = {
    json.asJsObject.getFields("username") match {
      case Seq(JsString(username)) =>
        username
      case _ => ""
    }
  }

  def uploadOnHdfs(image: String,userName: String) : Future[Unit] = Future {
    val request = "./script/upload.sh "+image+" "+userName
    val script =  request !!
  }

  override def receive: Receive = {

    case request: GetSaveRequest =>
      println("Received GetSaveRequest")
      val imageName = getImageName(request.imageName)
      val username = getUserName(request.imageName)
      uploadOnHdfs(imageName,username)
      sender() ! SaveResponse("{\"satus\":\"image found, wait for the upload\"}".parseJson)
  }
}

object SaveRequestHandler {
  def props(): Props = {
    Props(classOf[SaveRequestHandler])
  }
}
