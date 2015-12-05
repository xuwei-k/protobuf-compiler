package protobuf_compiler

import play.api.libs.json.{Json, OFormat}
import play.jsonext.CaseClassFormats

final case class GenerateRequest(files: List[ProtoFile], options: List[String]) {
  override def toString = jsonString
  def jsonString: String = Json.prettyPrint(GenerateRequest.format.writes(this))
}

object GenerateRequest extends ((List[ProtoFile], List[String]) => GenerateRequest){
  implicit val format: OFormat[GenerateRequest] =
    CaseClassFormats.apply2(this, unapply _)("files", "options")
}
