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
import java.nio.file.{Files, Paths, StandardCopyOption}

import services.tiles.models._
import services.tiles.messages.TilesMessages._

class TilesRequestHandler extends Actor with ActorLogging {

  private def prepareData(json: JsValue): Map[String, String] = {
    json.convertTo[Map[String,String]]
  }

  private def fileExists(path: String): Boolean = {
    val file = new File(path)
    file.exists()
  }

  private def moveToPermanentPlace(currentPath: String, newPath: String): Unit = {
    val path = Files.move(
      Paths.get(currentPath),
      Paths.get(newPath),
      StandardCopyOption.REPLACE_EXISTING
    )
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

  private def launchGDAL2TILESCalculation(pathImage: String, father: ActorRef): Future[Unit] = {
    val future = Future {
      val stdout = new StringBuilder
      val stderr = new StringBuilder
      "python gdal2tiles-script/gdal2tiles.py " + pathImage + " tiles/tmp/tmpOutputImage" ! ProcessLogger(stdout append _, stderr append _)
      if (fileExists("tiles/tmp/tmpOutputImage")) {
        moveToPermanentPlace("tiles/tmp/tmpOutputImage","tiles/outputImage")
      }
    }
    father ! TilesResponse(CorrectTiles(pathImage,s"Currently computing on the image at path ${pathImage}").toJson)
    future
  }

  def generateTiles(pathImage: JsValue, father: ActorRef) = {
    getGDAL2TILES(father)
    val path = prepareData(pathImage)("path")
    if (fileExists(path)) {
      launchGDAL2TILESCalculation(path, father)
    } else {
      father ! TilesResponse(ErrorTiles(path, "This path does not exist. Please specifiy an existing path ...").toJson)
    }
  }

  def checkDownloadStatus(father: ActorRef) = {
    if (fileExists("tiles/tmp/tmpOutputImage")) {
      father ! ComputeStatusResponse(ComputeStatus("Tiles are currently being computed","CURRENTLY_BEING_COMPUTED").toJson)
    } else {
      father ! ComputeStatusResponse(ComputeStatus("Tiles are totally computed !","TOTALLY_COMPUTED").toJson)
    }

  }

  override def receive: Receive = {

    case GetTilesRequest(pathImage) =>
      println("Received GetTilesRequest")
      generateTiles(pathImage, sender())
    case GetComputeStatusRequest =>
      println("Received GetDownloadStatusRequest")
      checkDownloadStatus(sender())
  }
}

object TilesRequestHandler {
  def props(): Props = {
    Props(classOf[TilesRequestHandler])
  }
}
