package protobuf_compiler

import play.api.libs.json.{Json, OFormat}
import play.jsonext.CaseClassFormats

final case class GenerateRequest(files: List[ProtoFile], options: List[String], private val lang0: Set[Language]) {
  override def toString = jsonString
  def jsonString: String = Json.prettyPrint(GenerateRequest.format.writes(this))
  def lang: Set[Language] = {
    val add: Set[Language] = if(options.contains("java_conversions")) {
      Set(Language.Scala, Language.Java)
    } else {
      Set.empty
    }
    if(lang0.isEmpty) {
      add + Language.Scala
    } else {
      add ++ lang0
    }
  }
}

object GenerateRequest extends ((List[ProtoFile], List[String], Set[Language]) => GenerateRequest){
  implicit val format: OFormat[GenerateRequest] =
    CaseClassFormats.apply3(this, unapply _)("files", "options", "language")
}
