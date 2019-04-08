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

  def getGDAL2TILES(father: ActorRef): Unit = {
    if (!fileExists("gdal2tiles-script/gdal2tiles.py")) {
      Future {
        "git clone https://github.com/LPauzies/gdal2tiles-script.git" !
      } onFailure {
        case ex => father ! TilesThrowServerError()
      }
    }
  }

  private def launchGDAL2TILESCalculation(pathImage: String): Future[(String, String)] = Future {
    val stdout = new StringBuilder
    val stderr = new StringBuilder
    "python gdal2tiles-script/gdal2tiles.py " + pathImage + " outputImage" ! ProcessLogger(stdout append _, stderr append _)
    (stdout.toString, stderr.toString)
  }

  private def handleGDAL2TILESExceptions(path: String, data: (String, String), father: ActorRef) = {
    if (data._2.nonEmpty) {
      father ! TilesResponse(ErrorTiles(path,data._2).toJson)
    } else {
      father ! TilesResponse(CorrectTiles(path,s"Currently computing on the image at path ${path}").toJson)
    }
  }

  def generateTiles(pathImage: JsValue, father: ActorRef) = {
    getGDAL2TILES(father)
    val path = prepareData(pathImage)("path")
    if (fileExists(path)) {
      launchGDAL2TILESCalculation(path) onComplete {
        case Success(data) => handleGDAL2TILESExceptions(path, data, father)
        case Failure(ex) => father ! TilesThrowServerError()
      }
    } else {
      father ! TilesResponse(ErrorTiles(path, "This path does not exist. Please specifiy an existing path ...").toJson)
    }
  }

  override def receive: Receive = {

    case GetTilesRequest(pathImage) =>
      println("Received GetTilesRequest")
      generateTiles(pathImage, sender())
  }
}

object TilesRequestHandler {
  def props(): Props = {
    Props(classOf[TilesRequestHandler])
  }
}
