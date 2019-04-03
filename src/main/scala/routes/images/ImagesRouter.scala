package routes.images

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
import services.images.actors.ImagesRequestHandler
import services.images.messages.ImagesMessages._
import services.images.models._

trait ImagesRouter {

  implicit val timeout: Timeout = 5.seconds

  def imagesRequestHandler : ActorRef

  def getImages: Route =
    get {
      entity(as[JsValue]) { coords =>
        onSuccess(imagesRequestHandler ? GetImagesRequest(coords)) {
          case response: ImagesResponse =>
            complete(StatusCodes.OK, response.images)
          case _ =>
            complete(StatusCodes.InternalServerError)
        }
      }
    }

  def images: Route =
    pathPrefix("images") { // the products
      pathEndOrSingleSlash { // /product or /product/
        getImages
      }
    }
}
