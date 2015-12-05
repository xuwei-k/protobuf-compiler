package protobuf_compiler

import play.api.libs.json.{Json, OFormat}
import play.jsonext.CaseClassFormats

final case class ScalaFile(name: String, src: String) {
  override def toString = Json.prettyPrint(ScalaFile.format.writes(this))
}

object ScalaFile{
  implicit val format: OFormat[ScalaFile] =
    CaseClassFormats(apply _, unapply _)(
      "name", "src"
    )
}
