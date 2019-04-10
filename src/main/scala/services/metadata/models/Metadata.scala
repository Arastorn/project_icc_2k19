package services.metadata.models

import spray.json._
import DefaultJsonProtocol._

final case class PostMetadataRequestStatus(
  imgName: String,
  status: String,
  statusCode: String,
  elasticSearchInsertionResult: JsValue,
  metadata: JsValue
)

final case class PostMetadataRequestErrorElasticStatus(
  imgName: String,
  status: String,
  statusCode: String,
  elasticSearchError: JsValue
)

final case class PostMetadataRequestErrorStatus(
  imgName: String,
  status: String,
  errorCode: String
)

final case class ArchivingInfoMetadata(
  archiving_centre: String,
  archiving_time: String
) {
  def toJson = JsObject(
    "archiving_centre" -> JsString(archiving_centre),
    "archiving_time" -> JsString(archiving_time)
  )
}

final case class GeneralInfoMetadata(
  tile_id: String,
  datastrip_id: String,
  sensing_time: String,
  archiving_info: ArchivingInfoMetadata
) {
  def toJson = JsObject(
    "tile_id" -> JsString(tile_id),
    "datastrip_id" -> JsString(datastrip_id),
    "sensing_time" -> JsString(sensing_time),
    "archiving_info" -> archiving_info.toJson
  )
}

final case class GeometricInfoMetadata(
  horizontal_cs_name: String,
  horizontal_cs_code: String
) {
  def toJson = JsObject(
    "horizontal_cs_name" -> JsString(horizontal_cs_name),
    "horizontal_cs_code" -> JsString(horizontal_cs_code)
  )
}

final case class QualityIndicatorsInfoMetadata(
  cloudy_pixel_percentage: Double,
  degraded_msi_data_percentage: Double
) {
  def toJson = JsObject(
    "cloudy_pixel_percentage" -> JsNumber(cloudy_pixel_percentage),
    "degraded_msi_data_percentage" -> JsNumber(degraded_msi_data_percentage)
  )
}

final case class Metadata(
  general_info: GeneralInfoMetadata,
  geometric_info: GeometricInfoMetadata,
  quality_indicators_info: QualityIndicatorsInfoMetadata
)  {
  def toJson = JsObject(
    "general_info" -> general_info.toJson,
    "geometric_info" -> geometric_info.toJson,
    "quality_indicators_info" -> quality_indicators_info.toJson
  )
}

object PostMetadataRequestStatus extends DefaultJsonProtocol {
  implicit val productFormat = jsonFormat5(PostMetadataRequestStatus.apply)
}

object PostMetadataRequestErrorElasticStatus extends DefaultJsonProtocol {
  implicit val productFormat = jsonFormat4(PostMetadataRequestErrorElasticStatus.apply)
}

object PostMetadataRequestErrorStatus extends DefaultJsonProtocol {
  implicit val productFormat = jsonFormat3(PostMetadataRequestErrorStatus.apply)
}
