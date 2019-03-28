package routes.utm

// packages
import akka.pattern.ask
import akka.util.Timeout
import scala.concurrent.duration._
import akka.actor.{Props, ActorLogging, Actor, ActorRef, ActorSystem}
import akka.http.scaladsl.server.Route
import akka.http.scaladsl.model.StatusCodes
import akka.http.scaladsl.Http
import akka.http.scaladsl.model.{HttpEntity, ContentTypes}
import akka.http.scaladsl.server.Directives._
import akka.http.scaladsl.marshallers.sprayjson.SprayJsonSupport._
import spray.json._

// Services
import services.utm.actors.UtmRequestHandler
import services.utm.messages.UtmMessages._
import services.utm.models._

trait UtmRouter {

  implicit val timeout: Timeout = 5.seconds

  def utmRequestHandler : ActorRef

  def getUtm: Route = {
    get {
      entity(as[JsValue]) {
        text => onSuccess(utmRequestHandler ? UtmGetRequest(text)) {
          case response: UtmResponse =>
            complete(StatusCodes.OK,response.utm)
          case response: UtmResponseNotFound =>
            complete(StatusCodes.NotFound)
        }
      }
    }
  }

  def getUtmMGRS: Route = {
    get {
      entity(as[JsValue]) {
        text => onSuccess(utmRequestHandler ? UtmGetRequestMGRS(text)) {
          case response: UtmResponse =>
            complete(StatusCodes.OK,response.utm)
          case response: UtmResponseNotFound =>
            complete(StatusCodes.NotFound)
        }
      }
    }
  }

  def utm: Route =
    pathPrefix("utm") {
      pathEndOrSingleSlash {
        getUtm
      } ~ path("mgrs") {
        pathEndOrSingleSlash {
          getUtmMGRS
        }
      }
    }
}
