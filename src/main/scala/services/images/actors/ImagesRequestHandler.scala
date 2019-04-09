package services.images.actors

import akka.actor.{Actor, ActorLogging, Props}
import akka.actor.ActorSystem
import akka.stream.ActorMaterializer
import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.duration._
import scala.concurrent._
import spray.json._
import spray.json.DefaultJsonProtocol._
import scalaj.http._
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.GregorianCalendar
import java.io._
import java.nio.file.{Path,Paths, Files, StandardCopyOption}
import java.util.zip.ZipInputStream
import sys.process._
import scala.util.matching.Regex


import services.images.models._
import services.images.messages.ImagesMessages._

class ImagesRequestHandler extends Actor with ActorLogging{

  def getCoordsFromSw(jsonCoords: JsValue): (String,String) = {
    jsonCoords.asJsObject.getFields("sw") match {
      case Seq(sw) =>
        (sw.asJsObject.getFields("lng")(0).toString,sw.asJsObject.getFields("lat")(0).toString)
      case _ => ("","")
    }
  }

  def getCoordsFromNe(jsonCoords: JsValue): (String,String) = {
    jsonCoords.asJsObject.getFields("ne") match {
      case Seq(ne) =>
        (ne.asJsObject.getFields("lng")(0).toString,ne.asJsObject.getFields("lat")(0).toString)
      case _ => ("","")
    }
  }

  def getItemFromJsKey(json: JsValue, key: String): JsValue = {
    json.asJsObject.getFields(key) match {
      case Seq(item) =>
        item
      case _ => "{}".parseJson
    }
  }

  def parseBoundingBox(jsonCoords: JsValue): JsValue = {
    getItemFromJsKey(jsonCoords,"boundingbox")
  }

  def getDate(): String = {
    val format = new SimpleDateFormat("yyyy-MM-dd")
    val date = Calendar.getInstance()
    date.add(Calendar.DATE, -4)
    format.format(date.getTime())
  }

  def sendRequestOnPeps(ne:(String,String), sw:(String,String)): JsValue = {
    Http("https://peps.cnes.fr/resto/api/collections/S2ST/search.json").param("box", ne._1 + "," + ne._2 + "," + sw._1 + "," + sw._2).param("startDate",getDate()).header("Accept", "application/json").asString.body.parseJson
  }

  def getNeAndSw(jsonCoords: JsValue) : ((String,String),(String,String)) = {
    val coords = parseBoundingBox(jsonCoords)
    val ne = getCoordsFromNe(coords)
    val sw = getCoordsFromSw(coords)
    (ne,sw)
  }

  def getImagesUrl(coords: ((String,String),(String,String))): JsValue = {
    sendRequestOnPeps(coords._1,coords._2)
  }

  def getMostRecentImage(jsonFromPeps: JsValue): JsValue = {
    jsonFromPeps.asJsObject.getFields("features") match {
      case Seq(JsArray(features)) =>
        features(0)
      case _ => "{}".parseJson
    }
  }


  def getProperties(jsonImage: JsValue): JsValue = {
    getItemFromJsKey(jsonImage,"properties")
  }

  def getService(jsonProperties: JsValue): JsValue = {
    getItemFromJsKey(jsonProperties, "services")
  }

  def getDownload(jsonService: JsValue): JsValue = {
    getItemFromJsKey(jsonService,"download")
  }

  def getUrl(jsonDownload: JsValue): String = {
    jsonDownload.asJsObject.getFields("url") match {
      case Seq(JsString(url)) =>
        url
      case _ => ""
    }
  }

  def getUrlFromFirstImage(jsonArray: JsValue) = {
    val properties = getProperties(jsonArray)
    val service = getService(properties)
    val download = getDownload(service)
    getUrl(download)
  }

  def parseUrl(url: String): String = {
    url.split("/")(6)
  }

  def createScript(url: String) = {
    val script = new PrintWriter(new File("download/script.sh" ))
    val uri = parseUrl(url)
    val requete = "wget --quiet --method GET --header 'Authorization: Basic Ym91cmdlb2lzYUBlaXN0aS5ldTpBZHJpZW42Ng==' --header 'cache-control: no-cache' --output-document - " + url + " >> download/"+uri+".zip"
    script.write(requete)
    script.close
  }

  def unzip(zipFile: InputStream, destination: Path): Unit = {
    val zis = new ZipInputStream(zipFile)
    Stream.continually(zis.getNextEntry).takeWhile(_ != null).foreach { file =>
      if (!file.isDirectory) {
        val outPath = destination.resolve(file.getName)
        val outPathParent = outPath.getParent
        if (!outPathParent.toFile.exists()) {
          outPathParent.toFile.mkdirs()
        }

        val outFile = outPath.toFile
        val out = new FileOutputStream(outFile)
        val buffer = new Array[Byte](4096)
        Stream.continually(zis.read(buffer)).takeWhile(_ != -1).foreach(out.write(buffer, 0, _))
      }
    }
  }


  def getListOfDirectorys(dir: File):List[File] = dir.listFiles.filter(_.isDirectory).toList

  def getListOfFiles(dir: File):List[File] = dir.listFiles.filter(_.isFile).toList

  def getListOfEverything(dir: File):List[File] = dir.listFiles.toList

  def getFileListName(files: List[File]) =files.map(_.getName).toList

  def recursiveListFiles(f: File, r: Regex): Array[File] = {
    val these = f.listFiles
    val good = these.filter(f => r.findFirstIn(f.getName).isDefined)
    good ++ these.filter(_.isDirectory).flatMap(recursiveListFiles(_,r))
  }

  def getImgDataFolder(name: String) = {
    recursiveListFiles(Paths.get("download",name).toFile,"GRANULE".r).head
  }

  def getImage(name: String): Path = {
    val imgFolder = getImgDataFolder(name)
    getListOfDirectorys(imgFolder).head.toPath
    /*if(listOfFolder.length > 0)
    {
      getListOfFiles(listOfFolder.filter( f => "10".r.findFirstIn(f.getName).isDefined).head).filter(f => "TCI".r.findFirstIn(f.getName).isDefined).head
    }
    else
    {
      getListOfFiles(imgFolder).filter(f => "TCI".r.findFirstIn(f.getName).isDefined).head
    }*/
  }

  def copyImage(dest: String, src: String) = {
    new FileOutputStream(dest) getChannel() transferFrom( new FileInputStream(src) getChannel, 0, Long.MaxValue )
  }

  def moveImage(dest: Path, src: Path)= {
    Files.move(src, dest, StandardCopyOption.REPLACE_EXISTING)
  }

  def deleteRecursively(file: File): Unit = {
    if (file.isDirectory)
      file.listFiles.foreach(deleteRecursively)
    if (file.exists && !file.delete)
      throw new Exception(s"Unable to delete ${file.getAbsolutePath}")
  }

  def downloadImage(uri: String): Future[Unit] = Future {
    val script = "./download/script.sh" !!

    unzip(new FileInputStream("download/"+uri+".zip"),Paths.get("download",uri))
    //copyImage("images/"+uri,getImage(uri).toString)
    moveImage(Paths.get("images",uri),getImage(uri))
    deleteRecursively(Paths.get("download",uri+".zip").toFile)
    deleteRecursively(Paths.get("download",uri).toFile)
  }


  override def receive: Receive = {

    case request: GetImagesRequest =>
      println("Received GetImagesRequest")
      val coords = getNeAndSw(request.coords)
      if(coords._1._1 == "" || coords._1._2 == "" || coords._2._1 == "" || coords._2._2 == "") {
        sender() ! WrongJsonCoord("{\"status\": \"Wrong json format for coords\"}".parseJson)
      } else {
        val url = getUrlFromFirstImage(getMostRecentImage(getImagesUrl(getNeAndSw(request.coords))))
        if(url.length() > 15) {
          createScript(url)
          val uri = parseUrl(url)
          val downloadFiles = getFileListName(getListOfEverything(Paths.get("download/").toFile))
          val imagesFiles = getFileListName(getListOfEverything(Paths.get("images/").toFile))
          if(imagesFiles.contains(uri)) {
            sender() ! ImagesResponse(("{\"status\": \"Image already downloaded\",\"name\": \""+ uri +"\"}").parseJson)
          } else if(downloadFiles.contains(uri) || downloadFiles.contains(uri+".zip")) {
            sender() ! ImagesResponse(("{\"status\": \"Image is already downloading\",\"name\": \""+ uri +"\"}").parseJson)
          } else {
            downloadImage(uri)
            sender() ! ImagesResponse(("{\"status\": \"Images found, wait for the download\",\"name\": \""+ uri +"\"}").parseJson)
          }
        } else {
          sender() ! NoImageFound("{\"status\": \"No images found on peps\"}".parseJson)
        }
      }
  }
}

object ImagesRequestHandler {
  def props(): Props = {
    Props(classOf[ImagesRequestHandler])
  }
}
