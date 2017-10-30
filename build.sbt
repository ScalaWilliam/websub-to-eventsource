name := "wte"

scalaVersion := "2.12.4"

enablePlugins(PlayScala)

libraryDependencies ++= Seq(
  "com.typesafe.play" %% "play-iteratees" % "2.6.1",
  "com.typesafe.play" %% "play-iteratees-reactive-streams" % "2.6.1",
  guice,
  ws,
  "com.typesafe.akka" %% "akka-agent" % "2.5.6",
  "org.scalatest" %% "scalatest" % "3.0.4" % "test",
  "org.scala-lang.modules" %% "scala-async" % "0.9.7",
  "org.jsoup" % "jsoup" % "1.10.3"
)
