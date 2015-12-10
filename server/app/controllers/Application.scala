package controllers

import play.api.libs.json.{JsError, JsSuccess, Json}
import play.api.mvc._
import protobuf_compiler._

import scalaz._

object Application extends Controller {

  final val post = Action(BodyParsers.parse.tolerantJson){ request =>
    println(request.body)
    request.body.validate[GenerateRequest] match {
      case JsSuccess(generateRequest, _) =>
        Ok(Json.toJson(Core.compile(generateRequest)))
      case e: JsError =>
        BadRequest(e.toString)
    }
  }

  def gist(gistId: String, options: List[String]) = Action {
    compileFromGist(gistId, options) match {
      case \/-(result) =>
        if(result.error){
          BadRequest(result.message)
        }else{
          Ok(views.html.gist(Env.GistBaseURL + gistId, result.files))
        }
      case -\/(a) =>
        BadRequest(a.toString)
    }
  }

  def gistApi(gistId: String, options: List[String]) = Action {
    compileFromGist(gistId, options) match {
      case \/-(result) =>
        if(result.error){
          BadRequest(result.message)
        }else{
          Ok(Json.toJson(result))
        }
      case -\/(err) =>
        BadRequest(err.toString)
    }
  }

  final val get = Action { request =>
    Ok(views.html.main())
  }

  private[this] def compileFromGist(gistId: String, options: List[String]): httpz.Error \/ CompileResult = {
    println("options " + options)
    fetchGist(gistId).bimap(
      f = err => {
        err.httpOr((), _.printStackTrace())
        err
      },
      g = a => {
        val files = a.files.collect{ case (name, file) if name.endsWith(".proto") =>
          ProtoFile(name, file.content)
        }.toList
        Core.compile(GenerateRequest(files, options))
      }
    )
  }

  private[this] val fetchGistHeader: Map[String, String] =
    Env.GithubToken match {
      case Some(token) =>
        Map(("Authorization", "token " + token))
      case None =>
        Map.empty[String, String]
    }

  private def fetchGist(gistId: String): httpz.Error \/ GistResponse = {
    import httpz._

    val req = httpz.Request(
      url = Env.GithubApiBaseURL + "gists/" + gistId,
      headers = fetchGistHeader
    )

    val action = httpz.Core.json[GistResponse](req)
    println("start fetch gist " + gistId)
    val result = action.interpretBy(httpz.async.AsyncInterpreter.sequential.empty.interpreter)
    println(result)
    result
  }
}
