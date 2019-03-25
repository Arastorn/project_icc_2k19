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

// routes
import routes.Router

// services
import services.products.actors.ProductRequestHandler
import services.hiking.actors.HikingRequestHandler


object ApplicationServer extends App with Router {

  val host = "localhost"
  val port = 9000

  implicit val system = ActorSystem()
  implicit val materializer = ActorMaterializer()
  implicit val executionContext = system.dispatcher

  val productRequestHandler = system.actorOf(ProductRequestHandler.props(), "productRequestHandler")

  val hikingRequestHandler = system.actorOf(HikingRequestHandler.props(),"hikingRequestHandler")

  val bindingFuture = Http().bindAndHandle(route, host, port)
  println(s"Server online at http://${host}:${port}/")

}
