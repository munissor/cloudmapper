lazy val root = (project in file(".")).
  settings(
    name := "proxyapp2",
    version := "1.0",
    scalaVersion := "2.11.8",
    libraryDependencies ++= Seq(
      "com.fasterxml.jackson.core" % "jackson-databind" % "2.7.5",
      "com.fasterxml.jackson.core" % "jackson-annotations" % "2.7.5",
      "com.fasterxml.jackson.core" % "jackson-core" % "2.7.5",

      "org.apache.httpcomponents" % "httpclient" % "4.5.2"
    )
  )


