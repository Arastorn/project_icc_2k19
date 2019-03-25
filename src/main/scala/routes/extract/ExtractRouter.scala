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

  def welcomeOnApiPathExtract: Route = {
    path("extract") {
      get {
        complete(
          HttpEntity(
            ContentTypes.`text/html(UTF-8)`,
            "<html><body>Hello ! Welcome on the extract API</body></html>"
          )
        )
      }
    }
  }

}
