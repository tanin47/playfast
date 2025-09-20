lazy val root =
  Project("plugins", file(".")).aggregate(SbtSvelte, SbtPostcss).dependsOn(SbtSvelte, SbtPostcss)

lazy val SbtSvelte = RootProject(uri("https://github.com/tanin47/sbt-svelte.git#93392b81cead783513fa17d2b6c9f725458e5e95"))

lazy val SbtPostcss = RootProject(uri("https://github.com/tanin47/sbt-postcss.git#d3da0221aee0d24f87cf85a488e72eddcb84a7a2"))


addSbtPlugin("org.playframework" % "sbt-plugin" % "3.0.7")
addSbtPlugin("org.foundweekends.giter8" % "sbt-giter8-scaffold" % "0.17.0")
addSbtPlugin("com.github.sbt" % "sbt-digest" % "2.1.0")
addSbtPlugin("com.github.sbt" % "sbt-gzip" % "2.0.0")
addSbtPlugin("ch.epfl.scala" % "sbt-scalafix" % "0.14.3")
addSbtPlugin("org.scalameta" % "sbt-scalafmt" % "2.5.5")