package routes.extract

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
import services.extract.actors.ExtractRequestHandler
import services.extract.messages.ExtractMessages._
import services.extract.models._

trait ExtractRouter {

  implicit val timeout: Timeout = 5.seconds

  def extractRequestHandler: ActorRef

  def extractWithTextInHeader: Route = {
    get {
      entity(as[JsValue]) {
        text => onSuccess(extractRequestHandler ? ExtractTextInHeaderRequestPost(text)) {
          case response: ExtractResponse =>
            complete(StatusCodes.OK, response.text)
          case response: ExtractResponseNotFound =>
            complete(StatusCodes.NotFound)
        }
      }
    }
  }

  def extract: Route = {
    pathPrefix("extract") {
      pathEndOrSingleSlash {
        extractWithTextInHeader
      }
    }
  }

}
