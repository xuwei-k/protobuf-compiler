package controllers

import play.api.libs.json.{JsError, JsSuccess, Json}
import play.api.mvc._
import protobuf_compiler._

import scalaz._

object Application extends Controller {

  val webJarAssets = new WebJarAssets(
    play.api.http.LazyHttpErrorHandler,
    play.api.Play.current.configuration,
    play.api.Environment.simple(
      path = play.api.Play.current.path,
      mode = play.api.Play.current.mode
    )
  )

  final val post = Action(BodyParsers.parse.tolerantJson){ request =>
    println(request.body)
    request.body.validate[GenerateRequest] match {
      case JsSuccess(generateRequest, _) =>
        Ok(Json.toJson(Core.compile(generateRequest)))
      case e: JsError =>
        println(e)
        BadRequest(e.toString)
    }
  }

  def gist(gistId: String, options: List[String], lang: List[String]) = Action {
    compileFromGist(gistId, options, lang) match {
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

  def gistApi(gistId: String, options: List[String], lang: List[String]) = Action {
    compileFromGist(gistId, options, lang) match {
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

  private[this] def compileFromGist(gistId: String, options: List[String], lang: List[String]): httpz.Error \/ CompileResult = {
    println("options " + options)
    println("lang " + lang)
    fetchGist(gistId).bimap(
      f = err => {
        err.httpOr((), _.printStackTrace())
        err
      },
      g = a => {
        val files = a.files.collect{ case (name, file) if name.endsWith(".proto") =>
          ProtoFile(name, file.content)
        }.toList
        val languages = {
          if(lang.contains("all")) {
            Language.all
          } else {
            lang.flatMap(l => Language.map.get(l)).toSet
          }
        }
        Core.compile(GenerateRequest(files, options, languages))
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
