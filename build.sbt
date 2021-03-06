name := "slick-codegen-example"

scalaVersion := "2.13.5"

scalacOptions += "-deprecation"

val slickVersion = "3.3.3"

libraryDependencies ++= Seq(
  "com.typesafe.slick" %% "slick" % slickVersion,
  "com.typesafe.slick" %% "slick-codegen" % slickVersion,
  "org.slf4j" % "slf4j-nop" % "1.7.19",
  "com.h2database" % "h2" % "1.4.196"
)

sourceGenerators in Compile += slick.taskValue // Automatic code generation on build

lazy val slick = taskKey[Seq[File]]("Generate Tables.scala")
slick := {
  val dir = (sourceManaged in Compile) value
  val outputDir = dir / "slick"
  val url = "jdbc:h2:mem:test;INIT=runscript from 'src/main/sql/create.sql'" // connection info
  val jdbcDriver = "org.h2.Driver"
  val slickDriver = "slick.jdbc.H2Profile"
  val pkg = "demo"

  val cp = (dependencyClasspath in Compile) value
  val s = streams value

  runner.value.run("slick.codegen.SourceCodeGenerator",
    cp.files,
    Array(slickDriver, jdbcDriver, url, outputDir.getPath, pkg),
    s.log).failed foreach (sys error _.getMessage)

  val file = outputDir / pkg / "Tables.scala"

  Seq(file)
}
