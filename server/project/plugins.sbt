lazy val scalikejdbcVersion = "3.3.0"

addSbtPlugin("org.scalariform" % "sbt-scalariform" % "1.8.2")

addSbtPlugin("io.spray" % "sbt-revolver" % "0.9.1")

addSbtPlugin("io.github.davidmweber" % "flyway-sbt" % "5.0.0")

// twilio for code gen
resolvers += Resolver.bintrayRepo("twilio", "releases")
addSbtPlugin("com.twilio" % "sbt-guardrail" % "0.41.2")

// scalikejdbc code gen
addSbtPlugin("org.scalikejdbc" %% "scalikejdbc-mapper-generator" % "3.3.0")
