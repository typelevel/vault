addSbtPlugin("org.scala-js" % "sbt-scalajs" % "1.8.0")
resolvers += Resolver.sonatypeRepo("snapshots")
val sbtTypelevelVersion = "0.4-ceec860-SNAPSHOT"
addSbtPlugin("org.typelevel" % "sbt-typelevel" % sbtTypelevelVersion)
addSbtPlugin("org.typelevel" % "sbt-typelevel-site" % sbtTypelevelVersion)
