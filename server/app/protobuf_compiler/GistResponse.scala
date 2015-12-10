package protobuf_compiler

import argonaut.CodecJson
import httpz.JsonToString

final case class GistResponse(files: Map[String, Gist]) extends JsonToString[GistResponse]

object GistResponse{
  implicit val instance: CodecJson[GistResponse] =
    CodecJson.casecodec1(apply, unapply)("files")
}
