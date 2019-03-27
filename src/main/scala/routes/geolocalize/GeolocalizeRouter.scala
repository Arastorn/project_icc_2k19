package routes.geolocalize

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
import services.geolocalize.actors.GeolocalizeRequestHandler
import services.geolocalize.messages.GeolocalizeMessages._
import services.geolocalize.models._

trait GeolocalizeRouter {

  implicit val timeout: Timeout = 5.seconds

  def geolocalizeRequestHandler : ActorRef

  def getGeolocalize: Route =
    get {
      entity(as[JsValue]) { ensReport =>
        onSuccess(geolocalizeRequestHandler ? GetGeolocalizeRequest(ensReport)) {
          case response: GeolocalizeResponse =>
            complete(StatusCodes.OK, response.geolocalize)
          case response: GeolocalizeThrowServerNotFound =>
            complete(StatusCodes.NotFound)
          case response: GeolocalizeResponseNotFound =>
            complete(StatusCodes.NotFound, response.json)
          case _ =>
            complete(StatusCodes.InternalServerError)
        }
      }
    }

  def geolocalize: Route =
    pathPrefix("geolocalize") {
      pathEndOrSingleSlash {
        getGeolocalize
      }
    }
}
