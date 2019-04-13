package services.images.actors

import akka.actor.{Actor, ActorRef, ActorLogging, Props}
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
import scala.collection.mutable.ListBuffer


import services.images.models._
import services.images.messages.ImagesMessages._

class ImagesRequestHandler extends Actor with ActorLogging{

  // Récupération des coordonnées


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


  def getStringFromJsKey(json: JsValue, key: String): String = {
    json.asJsObject.getFields(key) match {
      case Seq(JsString(string)) =>
        string
      case _ => ""
    }
  }

  /*def getCoordsFromSw(jsonCoords: JsValue): (String,String) = {
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
  }*/

  def getCoordsFromNe(json: JsValue): (String,String) = {
    (getItemFromJsKey(getItemFromJsKey(json,"ne"),"lng").toString,getItemFromJsKey(getItemFromJsKey(json,"ne"),"lat").toString)
  }

  def getCoordsFromSw(json : JsValue): (String,String) = {
    (getItemFromJsKey(getItemFromJsKey(json,"sw"),"lng").toString,getItemFromJsKey(getItemFromJsKey(json,"sw"),"lat").toString)
  }

  def getStartDate(json: JsValue): String = {
    getStringFromJsKey(getItemFromJsKey(json,"date"),"start").toString
  }


  def getEndDate(json: JsValue): String = {
    getStringFromJsKey(getItemFromJsKey(json,"date"),"end").toString
  }


  def getDate(): String = {
    val format = new SimpleDateFormat("yyyy-MM-dd")
    val date = Calendar.getInstance()
    date.add(Calendar.DATE, -4)
    format.format(date.getTime())
  }


  def sendRequestOnPeps(ne:(String,String), sw:(String,String), date:(String,String)): JsValue = {
    Http("https://peps.cnes.fr/resto/api/collections/S2ST/search.json").param("box", ne._1 + "," + ne._2 + "," + sw._1 + "," + sw._2).param("startDate",date._1).param("completionDate",date._2).header("Accept", "application/json").asString.body.parseJson
  }


  def getNeAndSw(jsonCoords: JsValue) : ((String,String),(String,String)) = {
    val coords = parseBoundingBox(jsonCoords)
    val ne = getCoordsFromNe(coords)
    val sw = getCoordsFromSw(coords)
    (ne,sw)
  }


  def getImagesUrl(coords: ((String,String),(String,String)), date:(String,String)): JsValue = {
    sendRequestOnPeps(coords._1,coords._2,date)
  }


  // Récupération des URL
  def getMostRecentImage(jsonFromPeps: JsValue): JsValue = {
    jsonFromPeps.asJsObject.getFields("features") match {
      case Seq(JsArray(features)) =>
        features(0)
      case _ => "{}".parseJson
    }
  }


  def getImagesFeatures(jsonFromPeps: JsValue): Vector[JsValue] = {
    jsonFromPeps.asJsObject.getFields("features") match {
      case Seq(JsArray(features)) =>
        features
      case _ => Vector[JsValue]()
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


  def getUrlFromFeature(jsonArray: JsValue) = {
    val properties = getProperties(jsonArray)
    val service = getService(properties)
    val download = getDownload(service)
    getUrl(download)
  }


  def getUrlListFromFeatures(jsonVector: Vector[JsValue]) = {
    val list = new ListBuffer[String]()
    for(feature <- jsonVector) {
      list += getUrlFromFeature(feature)
    }
    list.toList
  }


  def parseUrl(url: String): String = {
    url.split("/")(6)
  }


  def listOfUrlToImagesName(list: List[String]) = {
    val imagesNameList = new ListBuffer[String]()
    for(url <- list) {
      imagesNameList += parseUrl(url)
    }
    imagesNameList.toList
  }


  // Download images
  def createScript(imageName: String) = {
    val script = new PrintWriter(new File("script/download.sh" ))
    val requete = "wget --quiet --method GET --header 'Authorization: Basic Ym91cmdlb2lzYUBlaXN0aS5ldTpBZHJpZW42Ng==' --header 'cache-control: no-cache' --output-document - https://peps.cnes.fr/resto/collections/S2ST/"+imageName+"/download >> download/"+imageName+".zip"
    script.write(requete)
    script.close
  }


  // Dézippage
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


  // déplacement de download vers Image
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
    recursiveListFiles(Paths.get("download",name).toFile,"IMG_DATA".r).head
  }


  def getMtdFile(name: String): String = {
    recursiveListFiles(Paths.get("download",name).toFile,"MTD_TL".r).head.toString
  }


  def getImageTciFile(name: String): String = {
    val imgFolder = getImgDataFolder(name)
    val listOfFolder = getListOfDirectorys(imgFolder)
    if(listOfFolder.length > 0)
    {
      getListOfFiles(listOfFolder.filter( f => "10".r.findFirstIn(f.getName).isDefined).head).filter(f => "TCI".r.findFirstIn(f.getName).isDefined).head.toString
    }
    else
    {
      getListOfFiles(imgFolder).filter(f => "TCI".r.findFirstIn(f.getName).isDefined).head.toString
    }
  }


  def copy(dest: String, src: String) = {
    new FileOutputStream(dest) getChannel() transferFrom( new FileInputStream(src) getChannel, 0, Long.MaxValue )
  }


  def copyImage(name: String) = {
    val imageFile = new File("images/"+name);
    if(!imageFile.isDirectory){
      imageFile.mkdir();
    }
    copy(imageFile.toString+"/MTD_TL.xml",getMtdFile(name))
    copy(imageFile.toString+"/"+name+"_TCI.jp2",getImageTciFile(name))
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


  def getListOfNotDownloaded(imageList: List[String]) = {
    val imagesFiles = getFileListName(getListOfEverything(Paths.get("images/").toFile))
    val downloadFiles = getFileListName(getListOfEverything(Paths.get("download/").toFile))
    val listNotDownload = new ListBuffer[String]()
    for(image <- imageList){
      if(!(imagesFiles.contains(image)) && !(downloadFiles.contains(image)) && !(downloadFiles.contains(image+".zip"))){
        listNotDownload += image
      }
    }
    listNotDownload.toList
  }


  def getListDownloading(imageList: List[String]) = {
    val downloadFiles = getFileListName(getListOfEverything(Paths.get("download/").toFile))
    val listDownloading = new ListBuffer[String]()
    for(image <- imageList){
      if(downloadFiles.contains(image) || downloadFiles.contains(image+".zip")){
        listDownloading += image
      }
    }
    listDownloading.toList
  }


  def getListDownloaded(imageList: List[String]) = {
    val imagesFiles = getFileListName(getListOfEverything(Paths.get("images/").toFile))
    val listDownloaded = new ListBuffer[String]()
    for(image <- imageList){
      if(imagesFiles.contains(image)){
        listDownloaded += image
      }
    }
    listDownloaded.toList
  }


  def downloadImage(uri: String) = {
    val script = "./script/download.sh" !!

    unzip(new FileInputStream("download/"+uri+".zip"),Paths.get("download",uri))
    copyImage(uri)
    deleteRecursively(Paths.get("download",uri+".zip").toFile)
    deleteRecursively(Paths.get("download",uri).toFile)
  }


  def downloadAllImages(listNotDownload: List[String]): Future[Unit] = Future {
    for(image <- listNotDownload) {
      createScript(image)
      downloadImage(image)
    }
  }


  // Responses
  def imageDownloadResponse(images: List[String], father: ActorRef) = {
    val imagesNotDownloaded = getListOfNotDownloaded(images)
    val imagesDownloading = getListDownloading(images)
    val imagesDownloaded = getListDownloaded(images)
    if(imagesDownloading.length == 0 && imagesNotDownloaded.length == 0) {
      father ! ImagesResponse(ImageFound("Image already downloaded",imagesDownloading,imagesNotDownloaded,imagesDownloaded).toJson)
    } else if(imagesDownloading.length > 0) {
      father ! ImagesResponse(ImageFound("Image are already downloading",imagesDownloading,imagesNotDownloaded,imagesDownloaded).toJson)
    } else {
      downloadAllImages(imagesNotDownloaded)
      father ! ImagesResponse(ImageFound("Images Found, starting the download",imagesDownloading,imagesNotDownloaded,imagesDownloaded).toJson)
    }
  }


  def imageResponse(json: JsValue, father: ActorRef) = {
    val coords = getNeAndSw(json)
    val date = (getStartDate(json), getEndDate(json))
    val imagesNameList = listOfUrlToImagesName(getUrlListFromFeatures(getImagesFeatures(getImagesUrl(coords,date))))
    if(imagesNameList.length > 0) {
      imageDownloadResponse(imagesNameList,father)
    } else {
      father ! NoImageFound("{\"status\": \"No images found on peps\"}".parseJson)
    }
  }


  def getImageResponse(json: JsValue,father: ActorRef) = {
    val coords = getNeAndSw(json)
    val date = (getStartDate(json), getEndDate(json))
    if(coords._1._1 == "{}" || coords._1._2 == "{}" || coords._2._1 == "{}" || coords._2._2 == "{}") {
      father ! WrongJsonCoord(WrongCoords("No coords found","BAD_REQUEST").toJson)
    }
    else if(date._1.length() == 0 || date._2.length == 0)
    {
      father ! WrongJsonCoord(WrongCoords("No start date found","BAD_REQUEST").toJson)
    }
    else
    {
      imageResponse(json,father)
    }
  }


  override def receive: Receive = {

    case request: GetImagesRequest =>
      println("Received GetImagesRequest")
      getImageResponse(request.coords,sender())
  }
}

object ImagesRequestHandler {
  def props(): Props = {
    Props(classOf[ImagesRequestHandler])
  }
}
