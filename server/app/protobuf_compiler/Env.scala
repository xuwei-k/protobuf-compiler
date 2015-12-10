package protobuf_compiler

object Env {

  val GithubToken: Option[String] = {
    val key = "GITHUB_TOKEN"
    System.getenv(key) match {
      case null =>
        println(s"$key is null")
        None
      case token =>
        Option(token)
    }
  }

  val GithubApiBaseURL: String = {
    val key = "GITHUB_API_BASE_URL"
    System.getenv(key) match {
      case null =>
        println(s"$key is null. use default value")
        "https://api.github.com/"
      case url =>
        println(s"$key is $url")
        url
    }
  }

  val GistBaseURL: String = {
    val key = "GIST_BASE_URL"
    System.getenv(key) match {
      case null =>
        println(s"$key is null. use default value")
        "https://gist.github.com/"
      case url =>
        println(s"$key is $url")
        url
    }
  }

}
