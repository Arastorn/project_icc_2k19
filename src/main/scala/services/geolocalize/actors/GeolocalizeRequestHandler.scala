package services.geolocalize.actors

// packages
import akka.actor.{Actor, ActorLogging, Props}
import spray.json._
import spray.json.DefaultJsonProtocol._
import scalaj.http._

// services
import services.geolocalize.models._
import services.geolocalize.messages.GeolocalizeMessages._

class GeolocalizeRequestHandler extends Actor with ActorLogging{


  def parseEnsToUri(jsonEns: JsValue): JsValue = {
    jsonEns.asJsObject.getFields("entities") match {
      case Seq(entities) =>
        print(entities)
        entities
      case _ => "{}".parseJson
    }
  }

  def parseJsonToStringList(jsonValue: JsValue): List[String] = {
    val toRemove = "[]".toSet
    jsonValue.toString.replaceAll("\"", "").filterNot(toRemove).split(",").toList
  }

  def sendRequestOnUri(uri: String): JsValue = {
    Http("http://fr.dbpedia.org/sparql").param("query","select distinct ?coord where { <" + uri + "> georss:point ?coord } LIMIT 100").header("Accept", "application/json").asString.body.parseJson
  }

  def parseSparql(jsonSparql : JsValue) = {
    jsonSparql.asJsObject.getFields("results")(0).asJsObject.getFields("bindings")(0)
  }

  def parseFalseJsValue(json: JsValue): JsValue = {
    json.toString.filterNot("[]".toSet).parseJson
  }

  def getCoordValue(json: JsValue): (String,String) = {
    val coord = parseFalseJsValue(json).asJsObject.getFields("coord")(0).asJsObject.getFields("value")(0).toString.replaceAll("\"", "").split(" ")
    (coord(0),coord(1))
  }

  def writeJsonCoord(uri: String, coord: (String,String)): JsValue = {
    JsObject(
        "longitude" -> JsString(coord._2),
        "latitude" -> JsString(coord._1),
        "uri" -> JsString(uri)
    )
  }


  def sendRequestOnAllUri(list : List[String]): (JsValue,Int) = {
    var coords = "{ \"coords\" :["
    for ( element <- list ) {
      val test = parseSparql(sendRequestOnUri(element))
        if (test.toString.length() > 2)
        {
          coords = coords + writeJsonCoord(element,getCoordValue(test)).toString + ","
        }
    }
    coords = coords.dropRight(1) + "]}"
    if(coords.length() < 15) {
      ("{ \"status\": \"no coordinates found\"}".parseJson,404)
    } else {
      (coords.parseJson,200)
    }
  }

  override def receive: Receive = {

    case request: GetGeolocalizeRequest =>
      println("Received GetGeolocalizeRequest")
      var list = parseEnsToUri(request.ensReport)
      if (list.toString.length() < 5) {
        sender() ! GeolocalizeResponseNotFound("{\"status\": \"no entities key found in json\"}".parseJson)
      } else {
        sendRequestOnAllUri(parseJsonToStringList(list))._2 match {
          case 404 => sender() ! GeolocalizeResponseNotFound(sendRequestOnAllUri(parseJsonToStringList(list))._1)
          case 200 => sender() ! GeolocalizeResponse(sendRequestOnAllUri(parseJsonToStringList(list))._1)
          case _ => sender() ! GeolocalizeThrowServerNotFound()
        }

      }
  }
}

object GeolocalizeRequestHandler {
  def props(): Props = {
    Props(classOf[GeolocalizeRequestHandler])
  }
}
