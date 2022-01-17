val Scala212 = "2.12.15"
val Scala213 = "2.13.7"
val Scala3 = "3.0.2"

ThisBuild / tlBaseVersion := "3.1"
ThisBuild / crossScalaVersions := Seq(Scala212, Scala3, Scala213)
ThisBuild / tlVersionIntroduced := Map("3" -> "3.0.3")
ThisBuild / licenses := List("MIT" -> url("http://opensource.org/licenses/MIT"))
ThisBuild / startYear := Some(2021)
ThisBuild / tlApiDocsUrl := Some(url("https://www.javadoc.io/doc/org.typelevel/vault_2.13/latest/org/typelevel/vault/"))

val JDK8 = JavaSpec.temurin("8")
val JDK11 = JavaSpec.temurin("11")
val JDK17 = JavaSpec.temurin("17")

ThisBuild / githubWorkflowJavaVersions := Seq(JDK8, JDK11, JDK17)

lazy val root = tlCrossRootProject.aggregate(core, docs)

lazy val core = crossProject(JSPlatform, JVMPlatform)
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
      "org.typelevel" %%% "munit-cats-effect-3" % munitCatsEffectV % Test
    )
  )

lazy val docs = project
  .in(file("site"))
  .settings(tlFatalWarningsInCi := false)
  .dependsOn(core.jvm)
  .enablePlugins(TypelevelSitePlugin)

val catsV = "2.7.0"
val catsEffectV = "3.3.4"
val disciplineMunitV = "1.0.9"
val scalacheckEffectV = "1.0.3"
val munitCatsEffectV = "1.0.7"
val kindProjectorV = "0.13.2"

// Scalafmt
addCommandAlias("fmt", "; Compile / scalafmt; Test / scalafmt; scalafmtSbt")
addCommandAlias("fmtCheck", "; Compile / scalafmtCheck; Test / scalafmtCheck; scalafmtSbtCheck")
