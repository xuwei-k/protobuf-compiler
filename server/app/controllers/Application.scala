package controllers

import play.api.libs.json.{JsError, JsSuccess, Json}
import play.api.mvc._
import protobuf_compiler._

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

  final val get = Action { request =>
    Ok(views.html.main())
  }

}
