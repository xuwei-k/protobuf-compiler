scalacOptions ++= (
  "-deprecation" ::
  "-unchecked" ::
  "-Xlint" ::
  "-language:existentials" ::
  "-language:higherKinds" ::
  "-language:implicitConversions" ::
  "-Yno-adapted-args" ::
  Nil
)

addSbtPlugin("com.typesafe.play" % "sbt-plugin" % "2.5.15")

addSbtPlugin("com.heroku" % "sbt-heroku" % "0.5.4")

fullResolvers ~= {_.filterNot(_.name == "jcenter")}
