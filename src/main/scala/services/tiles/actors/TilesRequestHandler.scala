package services.tiles.actors

import akka.actor.{Actor, ActorRef, ActorLogging, Props}
import spray.json._
import spray.json.DefaultJsonProtocol._
import sys.process._
import scala.util.{Success, Failure}
import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.duration._
import scala.concurrent._
import java.io._
import java.nio.file.{Files, Path, Paths, StandardCopyOption}
import util.Try

import services.tiles.models._
import services.tiles.messages.TilesMessages._

class TilesRequestHandler extends Actor with ActorLogging {

  private def prepareData(json: JsValue): Map[String, String] = {
    json.convertTo[Map[String,String]]
  }

  private def fileRemove(path: String): Unit = {
    val file = new File(path)
    if (file.exists()) {
      file.delete()
    }
  }

  private def fileExists(path: String): Boolean = {
    val file = new File(path)
    file.exists()
  }

  private def isDirectory(path: String): Boolean = {
    val file = new File(path)
    file.isDirectory()
  }

  private def isAlreadyTiled(imgName: String): Boolean = {
    val file = s"images/${imgName}/tiles"
    fileExists(file) && isDirectory(file)
  }

  private def deleteRecursively(file: File): Unit = {
    if (file.isDirectory)
      file.listFiles.foreach(deleteRecursively)
    if (file.exists && !file.delete)
      throw new Exception(s"Unable to delete ${file.getAbsolutePath}")
  }

  private def rename(oldPath: String, newPath: String): Boolean = Try(new File(oldPath).renameTo(new File(newPath))).getOrElse(false)

  private def getListOfDirectories(dir: File): List[File] = dir.listFiles.filter(_.isDirectory).toList

  private def moveToPermanentPlace(currentPath: String, newPath: String): Unit = {
    val path = Files.move(
      Paths.get(currentPath),
      Paths.get(newPath),
      StandardCopyOption.REPLACE_EXISTING
    )
    deleteRecursively(new File(currentPath))
  }

  def getGDAL2TILES(father: ActorRef): Unit = {
    if (!fileExists("gdal2tiles-script/gdal2tiles.py")) {
      Future {
        "git clone https://github.com/LPauzies/gdal2tiles-script.git" !
      } onFailure {
        case ex => father ! TilesThrowServerError()
      }
    }
  }

  private def launchGDAL2TILESCalculation(pathImage: String, imgName: String): Future[Unit] = Future {
    val stdout = new StringBuilder
    val stderr = new StringBuilder
    val tmp = s"images/${imgName}/tmp_tiles"
    "python gdal2tiles-script/gdal2tiles.py " + pathImage + " " + tmp ! ProcessLogger(stdout append _, stderr append _)
    if (fileExists(tmp)) {
      rename(tmp,s"images/${imgName}/tiles")
    }
  }

  private def getTCIImgPath(imgDataPath: File): String = {
    def getListOfFiles(dir: File):List[File] = dir.listFiles.filter(_.isFile).toList
    getListOfFiles(imgDataPath).filter(f => "TCI".r.findFirstIn(f.getName).isDefined).head.toString
  }

  private def getTCIPath(pathFull: String): String = {
    var path = "";
    val mandatoryPath = pathFull
    if (fileExists(mandatoryPath) && isDirectory(mandatoryPath)) {
      val imgDataDir = new File(mandatoryPath)
      val originPathImg = getTCIImgPath(imgDataDir)
      val filename = originPathImg.split("/").toList.last
      path = s"${pathFull}/${filename}"
    }
    path
  }

  def generateTiles(pathImage: JsValue, father: ActorRef) = {
    getGDAL2TILES(father)
    val name = prepareData(pathImage)("name")
    val pathfull = "images/" + name
    if (fileExists(pathfull)) {
      if (isDirectory(pathfull)) {
        val pathTCI = getTCIPath(pathfull)
        if (pathTCI.nonEmpty) {
          if (isAlreadyTiled(name)) {
            father ! TilesResponse(CorrectTiles(name, s"images/${name}/tiles", s"Tiles have been already computed for this image").toJson)
          } else {
            launchGDAL2TILESCalculation(pathTCI, name)
            father ! TilesResponse(CorrectTiles(name, s"images/${name}/tiles", s"Currently computing on the image at path ${pathTCI}").toJson)
          }
        } else {
          father ! TilesResponse(ErrorTiles(pathfull, "This path is not poiting a directory that contains available data. Please specifiy a correct image name ...").toJson)
        }
      } else {
        father ! TilesResponse(ErrorTiles(pathfull, "This path is not poiting a directory. Please specifiy a correct image name ...").toJson)
      }
    } else {
      father ! TilesResponse(ErrorTiles(pathfull, "This path does not exist. Please specifiy an image name that is already downloaded ...").toJson)
    }
  }

  def checkDownloadStatus(imgName: String, father: ActorRef) = {
    if (fileExists(s"images/${imgName}/tmp_tiles") && isDirectory(s"images/${imgName}/tmp_tiles")) {
      father ! ComputeStatusResponse(ComputeStatus(imgName,"Tiles are currently being computed","CURRENTLY_BEING_COMPUTED").toJson)
    } else if (fileExists(s"images/${imgName}/tiles") && isDirectory(s"images/${imgName}/tiles")) {
      father ! ComputeStatusResponse(ComputeStatus(imgName,"Tiles are totally computed !","TOTALLY_COMPUTED").toJson)
    } else {
      father ! ComputeStatusResponse(ComputeStatus(imgName,"This image is not referenced","IMAGE_NOT_REFERENCED").toJson)
    }
  }

  override def receive: Receive = {

    case GetTilesRequest(pathImage) =>
      println("Received GetTilesRequest")
      generateTiles(pathImage, sender())
    case GetComputeStatusRequest(imgName) =>
      println("Received GetDownloadStatusRequest")
      checkDownloadStatus(imgName,sender())
  }
}

object TilesRequestHandler {
  def props(): Props = {
    Props(classOf[TilesRequestHandler])
  }
}
