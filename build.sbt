val Scala212 = "2.12.17"
val Scala213 = "2.13.11"
val Scala3 = "3.3.0"

ThisBuild / tlBaseVersion := "3.5"
ThisBuild / crossScalaVersions := Seq(Scala212, Scala3, Scala213)
ThisBuild / tlVersionIntroduced := Map("3" -> "3.0.3")
ThisBuild / tlMimaPreviousVersions ~= (_.filterNot(_ == "3.2.0"))
ThisBuild / licenses := List("MIT" -> url("http://opensource.org/licenses/MIT"))
ThisBuild / startYear := Some(2021)
ThisBuild / tlSiteApiUrl := Some(url("https://www.javadoc.io/doc/org.typelevel/vault_2.13/latest/org/typelevel/vault/"))

ThisBuild / developers := List(
  tlGitHubDev("christopherdavenport", "Christopher Davenport")
)

val JDK8 = JavaSpec.temurin("8")
val JDK17 = JavaSpec.temurin("17")

ThisBuild / githubWorkflowJavaVersions := Seq(JDK8, JDK17)

lazy val root = tlCrossRootProject.aggregate(core)

lazy val core = crossProject(JSPlatform, JVMPlatform, NativePlatform)
  .crossType(CrossType.Pure)
  .in(file("core"))
  .settings(
    name := "vault",
    libraryDependencies ++= Seq(
      "org.typelevel" %%% "cats-core" % catsV,
      "org.typelevel" %%% "cats-effect" % catsEffectV,
      "org.typelevel" %%% "cats-laws" % catsV % Test,
      "org.typelevel" %%% "discipline-munit" % disciplineMunitV % Test,
      "org.typelevel" %%% "scalacheck-effect-munit" % scalacheckEffectV % Test,
      "org.typelevel" %%% "munit-cats-effect" % munitCatsEffectV % Test
    )
  )
  .nativeSettings(
    tlVersionIntroduced := List("2.12", "2.13", "3").map(_ -> "3.2.2").toMap
  )

lazy val docs = project
  .in(file("site"))
  .settings(tlFatalWarningsInCi := false)
  .dependsOn(core.jvm)
  .enablePlugins(TypelevelSitePlugin)

val catsV = "2.9.0"
val catsEffectV = "3.5.0"
val disciplineMunitV = "2.0.0-M3"
val scalacheckEffectV = "2.0.0-M2"
val munitCatsEffectV = "2.0.0-M3"
val kindProjectorV = "0.13.2"

// Scalafmt
addCommandAlias("fmt", "; Compile / scalafmt; Test / scalafmt; scalafmtSbt")
addCommandAlias("fmtCheck", "; Compile / scalafmtCheck; Test / scalafmtCheck; scalafmtSbtCheck")
