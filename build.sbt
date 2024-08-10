import _root_.io.github.nafg.mergify.dsl._


name := "slick-codegen-example"


inThisBuild(List(
  scalaVersion := "2.13.14",
  githubWorkflowPublishTargetBranches := Seq(),
  githubWorkflowBuild += WorkflowStep.Sbt(List("run"))
))

mergifyExtraConditions := Seq(
  (Attr.Author :== "scala-steward") ||
    (Attr.Author :== "slick-scala-steward[bot]") ||
    (Attr.Author :== "renovate[bot]")
)

scalacOptions += "-deprecation"

val slickVersion = "3.5.1"

libraryDependencies ++= Seq(
  "com.typesafe.slick" %% "slick" % slickVersion,
  "com.typesafe.slick" %% "slick-codegen" % slickVersion,
  "org.slf4j" % "slf4j-nop" % "2.0.16",
  "com.h2database" % "h2" % "2.3.230"
)

(Compile / sourceGenerators) += slick.taskValue // Automatic code generation on build

lazy val slick = taskKey[Seq[File]]("Generate Tables.scala")
slick := {
  val dir = (Compile / sourceManaged).value
  val outputDir = dir / "slick"
  val url = "jdbc:h2:mem:test;INIT=runscript from 'src/main/sql/create.sql'" // connection info
  val jdbcDriver = "org.h2.Driver"
  val slickDriver = "slick.jdbc.H2Profile"
  val pkg = "demo"

  val cp = (Compile / dependencyClasspath).value
  val s = streams.value

  runner.value.run("slick.codegen.SourceCodeGenerator",
    cp.files,
    Array(slickDriver, jdbcDriver, url, outputDir.getPath, pkg),
    s.log).failed foreach (sys error _.getMessage)

  val file = outputDir / pkg / "Tables.scala"

  Seq(file)
}
