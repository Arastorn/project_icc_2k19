package routes.save

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
import services.save.actors.SaveRequestHandler
import services.save.messages.SaveMessages._
import services.save.models._

trait SaveRouter {

  implicit val timeout: Timeout = 5.seconds

  def saveRequestHandler : ActorRef

  def getSave: Route =
    get {
      onSuccess(saveRequestHandler ? GetSaveRequest) {
        case response: SaveResponse =>
          complete(StatusCodes.OK, response.save)
        case _ =>
          complete(StatusCodes.InternalServerError)
      }
    }

  def save: Route =
    pathPrefix("save") { // the products
      pathEndOrSingleSlash { // /product or /product/
        getSave
      }
    }
}
