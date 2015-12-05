package protobuf_compiler

import httpz.{Request, RequestF}
import play.api.libs.json.{JsResult, Reads, Json}

import scalaz.{EitherT, \/}
import scalaz.Free.FreeC

object ProtoHttpClient extends ProtoHttpClient("http://protobuf-compiler.herokuapp.com")

class ProtoHttpClient(url: String) {
  /**
      @example {{{
      val files = GenerateRequest(List(ProtoFile("hoge",""" syntax = "proto3"; service A{} """)), List("grpc"))
      ProtoHttpClient.compile(files).toOption.get.get.files.map(_.src).foreach(println)
      }}}
    */
  def compile(request: GenerateRequest): Throwable \/ JsResult[CompileResult] =
    action(request).interpretBy[scalaz.Id.Id](httpz.async.AsyncInterpreter.sequential.empty.interpreter)

  def action(request: GenerateRequest): EitherT[({type l[a] = FreeC[RequestF, a]})#l, Throwable, JsResult[CompileResult]] = {
    val req = Request(
      url = url,
      method = "POST",
      body = Option(request.jsonString.getBytes("UTF-8"))
    )
    httpz.Core.string(req).map(str =>
      implicitly[Reads[CompileResult]].reads(Json.parse(str))
    )
  }

}
