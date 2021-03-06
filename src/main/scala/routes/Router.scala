package routes

// packages
import akka.util.Timeout
import scala.concurrent.duration._
import akka.actor.{Props, ActorLogging, Actor, ActorRef, ActorSystem}
import akka.http.scaladsl.server.Route
import akka.http.scaladsl.model.StatusCodes
import akka.http.scaladsl.Http
import akka.http.scaladsl.model.{HttpEntity, ContentTypes}
import akka.http.scaladsl.server.Directives._
import scala.concurrent.{ExecutionContextExecutor, Future}
import ch.megard.akka.http.cors.scaladsl.CorsDirectives._

// Services
import services.products.actors.ProductRequestHandler
import services.products.messages.ProductMessages._
import services.products.models._

// routes
import routes.home.HomeRouter
import routes.products.ProductRouter
import routes.hiking.HikingRouter
import routes.extract.ExtractRouter
import routes.geolocalize.GeolocalizeRouter
import routes.utm.UtmRouter
import routes.images.ImagesRouter
import routes.tiles.TilesRouter
import routes.save.SaveRouter
import routes.metadata.MetadataRouter
import routes.elasticSearch.ElasticSearchRouter

trait Router extends HomeRouter with ProductRouter with HikingRouter with ExtractRouter with GeolocalizeRouter with UtmRouter with ImagesRouter with TilesRouter with SaveRouter with MetadataRouter with ElasticSearchRouter {

  override implicit val timeout: Timeout = 30.seconds
  implicit val system: ActorSystem

  def route: Route  = cors() {
    product ~
    hiking ~
    extract ~
    welcomeOnApiPath ~
    geolocalize ~
    utm ~
    images ~
    tiles ~
    save ~
    metadata ~
    elasticSearch
  }
}
