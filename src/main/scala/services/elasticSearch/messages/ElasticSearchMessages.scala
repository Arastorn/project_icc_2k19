package services.elasticSearch.messages

import services.elasticSearch.models._
import akka.actor._
import spray.json._

object ElasticSearchMessages {
  case class ElasticSearchResponse(status: JsValue)
  case class BoundingBoxQueryElasticSearch(boundingBox: JsValue)
  case class ElasticSearchQueryResponse(status: JsValue)
  case class ElasticSearchQueryThrowServerError()
  object GetElasticSearchRequest
}
