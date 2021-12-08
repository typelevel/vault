import sbtcrossproject.CrossPlugin.autoImport.{crossProject, CrossType}

val Scala212 = "2.12.15"
val Scala213 = "2.13.7"
val Scala3 = "3.0.2"

ThisBuild / baseVersion := "3.1"
ThisBuild / crossScalaVersions := Seq(Scala212, Scala213, Scala3)
ThisBuild / scalaVersion := crossScalaVersions.value.filter(_.startsWith("2.")).last
ThisBuild / publishFullName := "Christopher Davenport"
ThisBuild / publishGithubUser := "christopherdavenport"

ThisBuild / versionIntroduced := Map(
  // First versions after the Typelevel move
  "2.12" -> "2.1.0",
  "2.13" -> "2.1.0",
  "3.0.0-RC2" -> "2.1.9",
  "3.0.0-RC3" -> "2.1.10"
)

ThisBuild / spiewakMainBranches := Seq("main", "series/2.x")

enablePlugins(SonatypeCiReleasePlugin)

ThisBuild / githubWorkflowArtifactUpload := false

val Scala212Cond = s"matrix.scala == '$Scala212'"

def rubySetupSteps(cond: Option[String]) = Seq(
  WorkflowStep.Use(UseRef.Public("ruby", "setup-ruby", "v1"),
                   name = Some("Setup Ruby"),
                   params = Map("ruby-version" -> "2.6.0"),
                   cond = cond
  ),
  WorkflowStep.Run(List("gem install saas", "gem install jekyll -v 4.2.0"),
                   name = Some("Install microsite dependencies"),
                   cond = cond
  )
)

val JDK8 = JavaSpec.temurin("8")
val JDK11 = JavaSpec.temurin("11")
val JDK17 = JavaSpec.temurin("17")

ThisBuild / githubWorkflowJavaVersions := Seq(JDK8, JDK11, JDK17)

ThisBuild / githubWorkflowBuildPreamble ++=
  rubySetupSteps(Some(Scala212Cond))

ThisBuild / githubWorkflowBuild := Seq(WorkflowStep.Sbt(List("headerCheckAll", "test", "mimaReportBinaryIssues")),
                                       WorkflowStep.Sbt(List("docs/makeMicrosite"), cond = Some(Scala212Cond))
)

ThisBuild / githubWorkflowTargetBranches := List("*", "series/*")
ThisBuild / githubWorkflowTargetTags ++= Seq("v*")

// currently only publishing tags
ThisBuild / githubWorkflowPublishTargetBranches :=
  Seq(RefPredicate.StartsWith(Ref.Tag("v")))

ThisBuild / githubWorkflowPublishPreamble ++=
  rubySetupSteps(None)

ThisBuild / githubWorkflowPublish := Seq(
  WorkflowStep.Sbt(List("release")),
  WorkflowStep.Sbt(List(s"++${Scala212}", "docs/publishMicrosite"), name = Some(s"Publish microsite"))
)

lazy val vault = project
  .in(file("."))
  .enablePlugins(NoPublishPlugin)
  .settings(commonSettings, releaseSettings)
  .aggregate(coreJVM, coreJS)

lazy val core = crossProject(JSPlatform, JVMPlatform)
  .crossType(CrossType.Pure)
  .in(file("core"))
  .settings(commonSettings, releaseSettings)
  .settings(
    name := "vault"
  )
  .jsSettings(scalaJSLinkerConfig ~= (_.withModuleKind(ModuleKind.CommonJSModule)))

lazy val coreJVM = core.jvm
lazy val coreJS = core.js

lazy val docs = project
  .in(file("docs"))
  .settings(
    commonSettings,
    releaseSettings,
    micrositeSettings,
    publish / skip := true,
    githubWorkflowArtifactUpload := false
  )
  .dependsOn(coreJVM)
  .enablePlugins(MicrositesPlugin)
  .enablePlugins(MdocPlugin)

val catsV = "2.7.0"
val catsEffectV = "3.3.0"
val disciplineMunitV = "1.0.9"
val scalacheckEffectV = "1.0.3"
val munitCatsEffectV = "1.0.7"
val kindProjectorV = "0.13.2"

// General Settings
lazy val commonSettings = Seq(
  organization := "org.typelevel",
  libraryDependencies ++= (
    if (ScalaArtifacts.isScala3(scalaVersion.value)) Nil
    else
      Seq(
        compilerPlugin(("org.typelevel" % "kind-projector" % kindProjectorV).cross(CrossVersion.full))
      )
  ),
  libraryDependencies ++= Seq(
    "org.typelevel" %%% "cats-core" % catsV,
    "org.typelevel" %%% "cats-effect" % catsEffectV,
    "org.typelevel" %%% "cats-laws" % catsV % Test,
    "org.typelevel" %%% "discipline-munit" % disciplineMunitV % Test,
    "org.typelevel" %%% "scalacheck-effect-munit" % scalacheckEffectV % Test,
    "org.typelevel" %%% "munit-cats-effect-3" % munitCatsEffectV % Test
  ),
  // Cursed tags
  mimaPreviousArtifacts ~= (_.filterNot(m =>
    Set("2.1.1", "2.1.2", "2.1.3", "2.1.4", "2.1.5", "2.1.6", "2.1.11", "2.1.12").contains(m.revision)
  ))
)

lazy val releaseSettings = {
  Seq(
    Test / publishArtifact := false,
    scmInfo := Some(
      ScmInfo(
        url("https://github.com/typelevel/vault"),
        "git@github.com:typelevel/vault.git"
      )
    ),
    homepage := Some(url("https://github.com/typelevel/vault")),
    licenses := List("MIT" -> url("http://opensource.org/licenses/MIT"))
  )
}

lazy val micrositeSettings = {
  import microsites._
  Seq(
    mdocIn := sourceDirectory.value / "main" / "mdoc",
    micrositeName := "vault",
    micrositeDescription := "Type-safe, persistent storage for values of arbitrary types",
    micrositeAuthor := "Typelevel",
    micrositeGithubOwner := "typelevel",
    micrositeGithubRepo := "vault",
    micrositeUrl := "https://typelevel.org",
    micrositeBaseUrl := "/vault",
    micrositeDocumentationUrl := "https://www.javadoc.io/doc/typelevel/vault_2.13",
    micrositeFooterText := None,
    micrositeHighlightTheme := "atom-one-light",
    micrositePalette := Map(
      "brand-primary" -> "#3e5b95",
      "brand-secondary" -> "#294066",
      "brand-tertiary" -> "#2d5799",
      "gray-dark" -> "#49494B",
      "gray" -> "#7B7B7E",
      "gray-light" -> "#E5E5E6",
      "gray-lighter" -> "#F4F3F4",
      "white-color" -> "#FFFFFF"
    ),
    libraryDependencySchemes += "org.typelevel" %% "cats-effect" % VersionScheme.Always,
    libraryDependencies += "com.47deg" %% "github4s" % "0.28.4",
    micrositePushSiteWith := GitHub4s,
    micrositeGithubToken := sys.env.get("GITHUB_TOKEN"),
    micrositeExtraMdFiles := Map(
      file("CHANGELOG.md") -> ExtraMdFileConfig("changelog.md",
                                                "page",
                                                Map("title" -> "changelog",
                                                    "section" -> "changelog",
                                                    "position" -> "100"
                                                )
      ),
      file("CODE_OF_CONDUCT.md") -> ExtraMdFileConfig("code-of-conduct.md",
                                                      "page",
                                                      Map("title" -> "code of conduct",
                                                          "section" -> "code of conduct",
                                                          "position" -> "101"
                                                      )
      ),
      file("LICENSE") -> ExtraMdFileConfig("license.md",
                                           "page",
                                           Map("title" -> "license", "section" -> "license", "position" -> "102")
      )
    )
  )
}

// Scalafmt
addCommandAlias("fmt", "; Compile / scalafmt; Test / scalafmt; scalafmtSbt")
addCommandAlias("fmtCheck", "; Compile / scalafmtCheck; Test / scalafmtCheck; scalafmtSbtCheck")
