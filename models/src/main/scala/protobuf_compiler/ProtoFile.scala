package protobuf_compiler

import play.api.libs.json.{Json, OFormat}
import play.jsonext.CaseClassFormats

final case class ProtoFile(name: String, src: String) {
  override def toString = Json.prettyPrint(ProtoFile.format.writes(this))
}

object ProtoFile {
  implicit val format: OFormat[ProtoFile] =
    CaseClassFormats(apply _, unapply _)(
      "name", "src"
    )
}
