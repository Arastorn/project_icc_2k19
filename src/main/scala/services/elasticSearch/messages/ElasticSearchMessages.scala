package services.elasticSearch.messages

import services.elasticSearch.models._
import akka.actor._
import spray.json._

object ElasticSearchMessages {
  case class ElasticSearchResponse(elasticSearch: String)
  object GetElasticSearchRequest
}
