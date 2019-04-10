package services.save.actors

import akka.actor.{Actor, ActorRef, ActorLogging, Props}
import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.duration._
import scala.concurrent._
import akka.actor.ActorSystem
import akka.stream.ActorMaterializer
import spray.json._
import spray.json.DefaultJsonProtocol._
import sys.process._
import org.apache.commons._
import java.io._
import java.nio.file.{Path,Paths, Files, StandardCopyOption}


import services.save.models._
import services.save.messages.SaveMessages._

class SaveRequestHandler extends Actor with ActorLogging{

  def getListOfDirectorys(dir: File):List[File] = dir.listFiles.filter(_.isDirectory).toList

  def getImageName(json: JsValue): String = {
    json.asJsObject.getFields("imgName") match {
      case Seq(JsString(imgName)) =>
        imgName
      case _ => ""
    }
  }

  def uploadOnHdfs(image: String) : Future[Unit] = Future {
    val request = "./script/upload.sh "+image
    val stdout = new StringBuilder
    val stderr = new StringBuilder
    val script =  request !!

    removeTmpImage(image)
    removeTmp()
  }

  def createTmp() = {
    val tmp = new File("tmp");
    if(!tmp.isDirectory){
      tmp.mkdir();
    }
  }

  def deleteRecursively(file: File): Unit = {
    if (file.isDirectory)
      file.listFiles.foreach(deleteRecursively)
    if (file.exists && !file.delete)
      throw new Exception(s"Unable to delete ${file.getAbsolutePath}")
  }

  def removeTmpImage(image: String) = {
    val tmp = new File("tmp/"+image)
    deleteRecursively(tmp)
  }

  def removeTmp() = {
    val tmp = new File("tmp")
    if(tmp.isDirectory){
      if(getListOfDirectorys(tmp).length == 0){
        deleteRecursively(tmp)
      }
    }
  }

  def createImageDirectory(image: String) = {
    val imageTmp = new File("tmp/"+image);
    if(!imageTmp.isDirectory){
      imageTmp.mkdir();
    }
  }

  def createStatusDir(image: String) = {
    createTmp()
    createImageDirectory(image)
  }

  def checkTmpImage(image: String): Boolean = {
    val imageTmp = new File("tmp/"+image)
    imageTmp.isDirectory
  }

  def checkUpload(image: String): String = {
    val request = "./script/checkUpload.sh "+image
    val stdout = new StringBuilder
    val stderr = new StringBuilder
    val script =  request ! ProcessLogger(stdout append _, stderr append _)

    stderr.toString
  }

  def uploadImage(image : String, father: ActorRef) = {
    if(checkTmpImage(image)){
      father ! SaveUploading(CorrectSave("Already uploading please wait","SAVE_UPLOADING").toJson)
    } else {
      if(checkUpload(image).contains("No such file or directory")){
        createStatusDir(image)
        uploadOnHdfs(image)
        father ! SaveUploading(CorrectSave("Image found, wait for the upload","SAVE_START_UPLOADING").toJson)
      } else {
        father ! SaveUploaded(AlreadySaved("Already uploaded","http://factory02-studio.thai.cloud-torus.com/filebrowser/#/user/projet_ALL/images/"+image,"SAVE_IMAGE_UPLOADED").toJson)
      }
    }
  }

  def getSaveResponse(imageName: String, father: ActorRef) = {
    if(new File("images/"+imageName).isDirectory){
      uploadImage(imageName,father)
    } else {
      father ! WrongJson(WrongSave("No image found with this name","BAD_REQUEST").toJson)
    }
  }

  override def receive: Receive = {

    case request: GetSaveRequest =>
      println("Received GetSaveRequest")
      getSaveResponse(getImageName(request.imageName),sender())
  }
}

object SaveRequestHandler {
  def props(): Props = {
    Props(classOf[SaveRequestHandler])
  }
}
