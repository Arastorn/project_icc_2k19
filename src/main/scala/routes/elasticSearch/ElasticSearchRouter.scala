package routes.elasticSearch

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
import services.elasticSearch.actors.ElasticSearchRequestHandler
import services.elasticSearch.messages.ElasticSearchMessages._
import services.elasticSearch.models._

trait ElasticSearchRouter {

  implicit val timeout: Timeout = 5.seconds

  def elasticSearchRequestHandler : ActorRef

  def getElasticSearch: Route =
    post {
      entity(as[JsValue]) {
        boundingBox => onSuccess(elasticSearchRequestHandler ? BoundingBoxQueryElasticSearch(boundingBox)) {
          case response: ElasticSearchQueryResponse =>
            complete(StatusCodes.OK, response.status)
          case response: ElasticSearchQueryThrowServerError =>
            complete(StatusCodes.InternalServerError)
        }
      }
    }

  def elasticSearch: Route =
    pathPrefix("elasticSearch") { // the products
      pathEndOrSingleSlash { // /product or /product/
        getElasticSearch
      }
    }
}
