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
    get {
      onSuccess(tilesRequestHandler ? GetTilesRequest) {
        case response: TilesResponse =>
          complete(StatusCodes.OK, response.tiles)
        case _ =>
          complete(StatusCodes.InternalServerError)
      }
    }

  def tiles: Route =
    pathPrefix("tiles") { // the products
      pathEndOrSingleSlash { // /product or /product/
        getTiles
      }
    }
}
