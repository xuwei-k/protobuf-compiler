package protobuf_compiler

import play.api.libs.json.{Json, OFormat}
import play.jsonext.CaseClassFormats

final case class CompileResult(files: Seq[ScalaFile], message: String, error: Boolean) {
  override def toString = Json.prettyPrint(CompileResult.format.writes(this))
}

object CompileResult {
  implicit val format: OFormat[CompileResult] =
    CaseClassFormats(apply _, unapply _)(
      "files", "message", "error"
    )
}
