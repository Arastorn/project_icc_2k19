package services.elasticSearch.models

import spray.json._
import DefaultJsonProtocol._

final case class ElasticSearch(
  status: String,
  statusCode: String
)

final case class Coordinates(
  lat: String,
  lon: String
)

final case class ElasticSearchErrorStatus(
  host: String,
  status: String,
  statusCode: String
)

object ElasticSearchErrorStatus extends DefaultJsonProtocol {
  implicit val productFormat = jsonFormat3(ElasticSearchErrorStatus.apply)
}

object ElasticSearch extends DefaultJsonProtocol {
  implicit val productFormat = jsonFormat2(ElasticSearch.apply)
}

object Coordinates extends DefaultJsonProtocol {
  implicit val productFormat = jsonFormat2(Coordinates.apply)
}
