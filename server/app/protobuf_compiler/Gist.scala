package protobuf_compiler

import argonaut.CodecJson
import httpz.JsonToString

final case class Gist(content: String) extends JsonToString[Gist]

object Gist{
  implicit val instance: CodecJson[Gist] =
    CodecJson.casecodec1(apply, unapply)("content")
}
