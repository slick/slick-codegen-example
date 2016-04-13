val slickVersion = "3.1.1"

lazy val mainProject = Project(
  id="slick-codegen-example",
  base=file("."),
  settings = Defaults.coreDefaultSettings ++ Seq(
    scalaVersion := "2.11.8",
    libraryDependencies ++= List(
      "com.typesafe.slick" %% "slick" % slickVersion,
      "com.typesafe.slick" %% "slick-codegen" % slickVersion,
      "org.slf4j" % "slf4j-nop" % "1.7.19",
      "com.h2database" % "h2" % "1.4.191"
    ),
    slick <<= slickCodeGenTask, // register manual sbt command
    sourceGenerators in Compile <+= slickCodeGenTask // register automatic code generation on every compile, remove for only manual use
  )
)

// code generation task
lazy val slick = TaskKey[Seq[File]]("gen-tables")
lazy val slickCodeGenTask = (sourceManaged, dependencyClasspath in Compile, runner in Compile, streams) map { (dir, cp, r, s) =>
  val outputDir = (dir / "slick").getPath // place generated files in sbt's managed sources folder
  val url = "jdbc:h2:mem:test;INIT=runscript from 'src/main/sql/create.sql'" // connection info for a pre-populated throw-away, in-memory db for this demo, which is freshly initialized on every run
  val jdbcDriver = "org.h2.Driver"
  val slickDriver = "slick.driver.H2Driver"
  val pkg = "demo"
  toError(r.run("slick.codegen.SourceCodeGenerator", cp.files, Array(slickDriver, jdbcDriver, url, outputDir, pkg), s.log))
  val fname = outputDir + "/demo/Tables.scala"
  Seq(file(fname))
}
