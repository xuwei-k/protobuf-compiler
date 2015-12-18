package protobuf_compiler

import java.util.Locale

import play.api.libs.json._

sealed abstract class Language(val name: String, val extensions: Set[String]) extends Product with Serializable

object Language {
  case object Scala extends Language("scala", Set("scala"))
  case object Java extends Language("java", Set("java"))
  case object Python extends Language("python", Set("py"))
  case object CSharp extends Language("csharp", Set("cs"))
  case object CPP extends Language("cpp", Set("cpp", "h", "cc"))
  case object Ruby extends Language("ruby", Set("rb"))
  case object Objc extends Language("objc", Set("m", "h"))
  case object PHP extends Language("php", Set("php"))

  val all: Set[Language] = Set(
    Scala, Java, Python, CSharp, CPP, Ruby, Objc, PHP
  )

  val map: Map[String, Language] =
    all.map(l => l.name -> l).toMap

  implicit val instance: Format[Language] =
    Format(
      Reads{
        case JsString(s) =>
          map.get(s.toLowerCase(Locale.ENGLISH)) match {
            case Some(l) =>
              JsSuccess(l)
            case None =>
              JsError(s + "is not valid language")
          }
        case s =>
          JsError(s"expect JsString, actual $s")
      },
      Writes[Language](l => JsString(l.name))
    )
}
