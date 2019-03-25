package services

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

import routes.Router
import services.products.actors.ProductRequestHandler
import services.extract.actors.ExtractRequestHandler


object ApplicationServer extends App with Router {

  val host = "localhost"
  val port = 9000

  implicit val system = ActorSystem()
  implicit val materializer = ActorMaterializer()
  implicit val executionContext = system.dispatcher

  val productRequestHandler = system.actorOf(ProductRequestHandler.props(), "productRequestHandler")
  val extractRequestHandler = system.actorOf(ExtractRequestHandler.props(), "extractRequestHandler")

  val bindingFuture: Future[Http.ServerBinding] = Http().bindAndHandle(route, host, port)
  println(s"Server online at http://${host}:${port}/")

  StdIn.readLine()
  // Unbind from the port and shut down when done
  bindingFuture
    .flatMap(_.unbind())
    .onComplete(_ => system.terminate())

}
