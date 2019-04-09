package routes.tiles

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
import services.tiles.actors.TilesRequestHandler
import services.tiles.messages.TilesMessages._
import services.tiles.models._

trait TilesRouter {

  implicit val timeout: Timeout = 5.seconds

  def tilesRequestHandler : ActorRef

  def getTiles: Route =
    post {
      entity(as[JsValue]) {
        pathImage => onSuccess(tilesRequestHandler ? GetTilesRequest(pathImage)) {
        case response: TilesResponse =>
          complete(StatusCodes.OK, response.pathTiles)
        case response: TilesThrowServerError =>
          complete(StatusCodes.InternalServerError)
        }
      }
    }

  def getComputeStatus: Route =
    get {
      onSuccess(tilesRequestHandler ? GetComputeStatusRequest) {
        case response: ComputeStatusResponse =>
          complete(StatusCodes.OK, response.downloadStatus)
        case response: ComputeStatusThrowServerError =>
          complete(StatusCodes.InternalServerError)
      }
    }

  def tiles: Route =
    pathPrefix("tiles") {
      pathEndOrSingleSlash {
        getTiles
      } ~ pathPrefix("status") {
        pathEndOrSingleSlash {
          getComputeStatus
        }
      }
    }

}
