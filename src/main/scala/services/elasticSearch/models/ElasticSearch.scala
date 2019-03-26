package services.elasticSearch.models

import spray.json._
import DefaultJsonProtocol._

final case class ElasticSearch(
  namme: String,
  texte: String
)

object ElasticSearch extends DefaultJsonProtocol {
  implicit val productFormat = jsonFormat2(ElasticSearch.apply)
}
