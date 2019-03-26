package services

// packages
import akka.actor.ActorSystem
import akka.stream.scaladsl._
import akka.util.ByteString
import akka.http.scaladsl.Http
import akka.http.scaladsl.model.{HttpEntity, ContentTypes}
import akka.http.scaladsl.server.Directives._
import akka.stream.ActorMaterializer
import akka.http.scaladsl.marshallers.sprayjson.SprayJsonSupport._
import scala.io.StdIn
import scala.concurrent.duration._
import scala.concurrent._

// routes
import routes.Router

// services
import services.products.actors.ProductRequestHandler
import services.hiking.actors.HikingRequestHandler
import services.extract.actors.ExtractRequestHandler
import services.geolocalize.actors.GeolocalizeRequestHandler
import services.utm.actors.UtmRequestHandler
import services.images.actors.ImagesRequestHandler
import services.tiles.actors.TilesRequestHandler
import services.save.actors.SaveRequestHandler
import services.metadata.actors.MetadataRequestHandler
import services.elasticSearch.actors.ElasticSearchRequestHandler

object ApplicationServer extends App with Router {

  val host = "localhost"
  val port = 9001

  implicit val system = ActorSystem()
  implicit val materializer = ActorMaterializer()
  implicit val executionContext = system.dispatcher

  val productRequestHandler = system.actorOf(ProductRequestHandler.props(), "productRequestHandler")
  val extractRequestHandler = system.actorOf(ExtractRequestHandler.props(), "extractRequestHandler")

  val hikingRequestHandler = system.actorOf(HikingRequestHandler.props(),"hikingRequestHandler")

  val geolocalizeRequestHandler = system.actorOf(GeolocalizeRequestHandler.props(),"geolocalizeRequestHandler")

  val utmRequestHandler = system.actorOf(UtmRequestHandler.props(),"utmRequestHandler")

  val imagesRequestHandler = system.actorOf(ImagesRequestHandler.props(),"imagesRequestHandler")

  val tilesRequestHandler = system.actorOf(TilesRequestHandler.props(),"tilesRequestHandler")

  val saveRequestHandler = system.actorOf(SaveRequestHandler.props(),"saveRequestHandler")

  val metadataRequestHandler = system.actorOf(MetadataRequestHandler.props(),"metadataRequestHandler")

  val elasticSearchRequestHandler = system.actorOf(ElasticSearchRequestHandler.props(),"elasticSearchRequestHandler")

  val bindingFuture: Future[Http.ServerBinding] = Http().bindAndHandle(route, host, port)

  println(s"Server online at http://${host}:${port}/")

  StdIn.readLine()
  // Unbind from the port and shut down when done
  bindingFuture
    .flatMap(_.unbind())
    .onComplete(_ => system.terminate())

}
