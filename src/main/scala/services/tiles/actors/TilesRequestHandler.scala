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

import services.tiles.models._
import services.tiles.messages.TilesMessages._

class TilesRequestHandler extends Actor with ActorLogging {

  private def prepareData(json: JsValue): Map[String, String] = {
    json.convertTo[Map[String,String]]
  }

  private def fileRemove(path: String): Unit = {
    val file = new File(path)
    file.delete()
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
    val file = s"tiles/${imgName}"
    fileExists(file) && isDirectory(file)
  }

  private def moveToPermanentPlace(currentPath: String, newPath: String): Unit = {
    val path = Files.move(
      Paths.get(currentPath),
      Paths.get(newPath),
      StandardCopyOption.REPLACE_EXISTING
    )
    fileRemove(currentPath)
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
    val tmp = s"tiles/tmp/tmp_${imgName}"
    "python gdal2tiles-script/gdal2tiles.py " + pathImage + " " + tmp ! ProcessLogger(stdout append _, stderr append _)
    if (fileExists(tmp)) {
      moveToPermanentPlace(tmp,s"tiles/${imgName}")
    }
  }

  private def getTCIPathFromIMGData(imgDataPath: File, accuracy: Int): String = {
    def getListOfDirectories(dir: File): List[File] = dir.listFiles.filter(_.isDirectory).toList
    def getListOfFiles(dir: File):List[File] = dir.listFiles.filter(_.isFile).toList
    val listOfDirectoriesAccuracies = getListOfDirectories(imgDataPath)
    if (listOfDirectoriesAccuracies.length > 0) {
      getListOfFiles(listOfDirectoriesAccuracies.filter(f => accuracy.toString.r.findFirstIn(f.getName).isDefined).head).filter(f => "TCI".r.findFirstIn(f.getName).isDefined).head.toString
    } else {
      ""
    }
  }

  private def getTCIPath(pathFull: String, accuracy: Int) = {
    var path = "";
    //Same architecture
    val mandatoryPath = pathFull + "/IMG_DATA"
    if (fileExists(mandatoryPath) && isDirectory(mandatoryPath)) {
      val imgDataDir = new File(mandatoryPath)
      path = getTCIPathFromIMGData(imgDataDir,accuracy)
    }
    path
  }

  def generateTiles(pathImage: JsValue, father: ActorRef) = {
    getGDAL2TILES(father)
    val name = prepareData(pathImage)("name")
    val pathfull = "images/" + name
    if (fileExists(pathfull)) {
      if (isDirectory(pathfull)) {
        val pathTCI = getTCIPath(pathfull, 10)
        if (pathTCI.nonEmpty) {
          if (isAlreadyTiled(name)) {
            father ! TilesResponse(CorrectTiles(name, s"tiles/${name}", s"Tiles have been already computed for this image").toJson)
          } else {
            launchGDAL2TILESCalculation(pathTCI, name)
            father ! TilesResponse(CorrectTiles(name, s"tiles/${name}", s"Currently computing on the image at path ${pathTCI}").toJson)
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
    if (fileExists(s"tiles/${imgName}") && isDirectory(s"tiles/${imgName}")) {
      father ! ComputeStatusResponse(ComputeStatus(imgName,"Tiles are totally computed !","TOTALLY_COMPUTED").toJson)
    } else if (fileExists(s"tiles/tmp/tmp_${imgName}")) {
      father ! ComputeStatusResponse(ComputeStatus(imgName,"Tiles are currently being computed","CURRENTLY_BEING_COMPUTED").toJson)
    } else {
      father ! ComputeStatusResponse(ComputeStatus(imgName,"The tiles for this image do not exists","IMAGE_NOT_TILED").toJson)
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
