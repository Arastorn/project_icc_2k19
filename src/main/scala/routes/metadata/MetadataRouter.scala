package routes.metadata

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
import services.metadata.actors.MetadataRequestHandler
import services.metadata.messages.MetadataMessages._
import services.metadata.models._

trait MetadataRouter {

  implicit val timeout: Timeout = 5.seconds

  def metadataRequestHandler : ActorRef

  def putMetadata: Route =
    post {
      entity(as[JsValue]) {
        imgJson => onSuccess(metadataRequestHandler ? PutImgMetadataToElasticSearchRequest(imgJson)) {
          case response: MetadataResponse =>
            complete(StatusCodes.OK, response.status)
          case response: MetadataThrowServerError =>
            complete(StatusCodes.InternalServerError)
        }
      }
    }

  def metadata: Route =
    pathPrefix("metadata") { // the products
      pathEndOrSingleSlash { // /product or /product/
        putMetadata
      }
    }
}
