lazy val scalikejdbcVersion = "3.3.0"

addSbtPlugin("io.spray" % "sbt-revolver" % "0.9.1")

addSbtPlugin("io.github.davidmweber" % "flyway-sbt" % "5.0.0")

// twilio for code gen
resolvers += Resolver.bintrayRepo("twilio", "releases")
addSbtPlugin("com.twilio" % "sbt-guardrail" % "0.41.2")

// scalikejdbc code gen
addSbtPlugin("org.scalikejdbc" %% "scalikejdbc-mapper-generator" % "3.3.0")

addSbtPlugin("com.geirsson" % "sbt-scalafmt" % "1.5.1")

resolvers += Classpaths.sbtPluginReleases

addSbtPlugin("org.scoverage" % "sbt-scoverage" % "1.5.1")
