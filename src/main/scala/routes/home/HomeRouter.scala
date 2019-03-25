package routes.home

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
import scala.concurrent.{ExecutionContextExecutor, Future}

trait HomeRouter {

  def welcomeOnApiPath: Route =
    pathEndOrSingleSlash {
      complete(
        HttpEntity(
          ContentTypes.`text/plain(UTF-8)`,
          "<html><body>Hello ! Welcome on the product API</body></html>"
        )
      )
    }

}
