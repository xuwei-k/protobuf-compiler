val unusedWarningsSettings: Seq[Setting[_]] = {
  val unusedWarnings = (
    "-Ywarn-unused" ::
    "-Ywarn-unused-import" ::
    Nil
  )

  Seq(
    scalacOptions ++= unusedWarnings
  ) ++ Seq(Compile, Test).flatMap(c =>
    scalacOptions in (c, console) ~= {_.filterNot(unusedWarnings.toSet)}
  )
}

val baseSettings = Seq(
  scalaVersion := "2.11.8",
  licenses := Seq("MIT License" -> url("http://opensource.org/licenses/mit")),
  scalacOptions ++= (
    "-deprecation" ::
    "-unchecked" ::
    "-Xlint" ::
    "-language:existentials" ::
    "-language:higherKinds" ::
    "-language:implicitConversions" ::
    Nil
  ),
  herokuSkipSubProjects in Compile := false,
  fullResolvers ~= {_.filterNot(_.name == "jcenter")},
  javaOptions ++= sys.process.javaVmArguments.filter(
    a => Seq("-Xmx", "-Xms", "-XX").exists(a.startsWith)
  ),
  ivyScala := ivyScala.value map { _.copy(overrideScalaVersion = true) },
  shellPrompt := { state =>
    val branch = if(file(".git").exists){
      "git branch".lines_!.find{_.head == '*'}.map{_.drop(1)}.getOrElse("")
    }else ""
    Project.extract(state).currentRef.project + branch + " > "
  },
  libraryDependencies ++= (
    ("org.scalatest" %% "scalatest" % "2.2.6" % "test") ::
    Nil
  ),
  resolvers += Resolver.url("typesafe", url("http://repo.typesafe.com/typesafe/ivy-releases/"))(Resolver.ivyStylePatterns),
  resolvers += Opts.resolver.sonatypeReleases
)

val scalapbVersion = "0.5.22"

def module(id: String) = Project(id, file(id)).settings(
  baseSettings,
  name := s"protobuf-compiler-$id"
)

lazy val models = module("models").settings(
  unusedWarningsSettings,
  libraryDependencies ++= (
    ("com.github.xuwei-k" %% "play-json-extra" % "0.3.0") ::
    ("com.typesafe.play" %% "play-json" % play.core.PlayVersion.current) ::
    ("org.scalaz" %% "scalaz-core" % "7.1.7") ::
    Nil
  )
)

lazy val core = module("core").settings(
  unusedWarningsSettings,
  libraryDependencies ++= (
    ("com.trueaccord.scalapb" %% "compilerplugin" % scalapbVersion) ::
    ("com.trueaccord.scalapb" %% "scalapb-runtime" % scalapbVersion) ::
    ("com.github.os72" % "protoc-jar" % "3.0.0-b2") ::
    ("com.google.protobuf" % "protobuf-java" % "3.0.0-beta-2") ::
    ("org.scala-sbt" %% "io" % sbtVersion.value) ::
    Nil
  )
).dependsOn(
  models
)

val httpzAsync = "com.github.xuwei-k" %% "httpz-async" % "0.3.0"

lazy val client = module("client").settings(
  unusedWarningsSettings,
  libraryDependencies ++= (
    httpzAsync ::
    Nil
  )
).dependsOn(models)

lazy val server = module("server").enablePlugins(PlayScala).enablePlugins(HerokuPlugin).settings(
  baseSettings,
  herokuAppName in Compile := "protobuf-compiler",
  libraryDependencies ++= (
    httpzAsync ::
    ("org.webjars.bower" % "google-code-prettify" % "1.0.4") ::
    ("org.webjars" %% "webjars-play" % "2.4.0-2") ::
    Nil
  )
).dependsOn(core)
