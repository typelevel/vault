import sbtcrossproject.CrossPlugin.autoImport.{crossProject, CrossType}

val Scala212 = "2.12.12"

ThisBuild / baseVersion := "2.1"
ThisBuild / crossScalaVersions := Seq(Scala212, "2.13.3", "3.0.0-M2", "3.0.0-M3")
ThisBuild / scalaVersion := crossScalaVersions.value.filter(_.startsWith("2.")).last
ThisBuild / publishFullName := "Christopher Davenport"
ThisBuild / publishGithubUser := "christopherdavenport"

ThisBuild / versionIntroduced := Map(
  // First versions after the Typelevel move
  "2.12" -> "2.1.0",
  "2.13" -> "2.1.0",
  "3.0.0-M2" -> "2.1.0",
  "3.0.0-M3" -> "2.1.0",
)

ThisBuild / spiewakMainBranches := Seq("main", "series/2.x")

enablePlugins(SonatypeCiReleasePlugin)

val Scala212Cond = s"matrix.scala == '$Scala212'"

def rubySetupSteps(cond: Option[String]) = Seq(
  WorkflowStep.Use(
    "ruby", "setup-ruby", "v1",
    name = Some("Setup Ruby"),
    params = Map("ruby-version" -> "2.6.0"),
    cond = cond),

  WorkflowStep.Run(
    List(
      "gem install saas",
      "gem install jekyll -v 3.2.1"),
    name = Some("Install microsite dependencies"),
    cond = cond))

ThisBuild / githubWorkflowBuildPreamble ++=
  rubySetupSteps(Some(Scala212Cond))

ThisBuild / githubWorkflowBuild := Seq(
  WorkflowStep.Sbt(List("test", "mimaReportBinaryIssues")),

  WorkflowStep.Sbt(
    List("docs/makeMicrosite"),
    cond = Some(Scala212Cond)))

ThisBuild / githubWorkflowTargetTags ++= Seq("v*")

// currently only publishing tags
ThisBuild / githubWorkflowPublishTargetBranches :=
  Seq(RefPredicate.StartsWith(Ref.Tag("v")))

ThisBuild / githubWorkflowPublish := Seq(
  WorkflowStep.Sbt(List("release")),
  WorkflowStep.Sbt(List("docs/publishMicrosite"),
    name = Some(s"Publish microsite")),
)

lazy val vault = project.in(file("."))
  .disablePlugins(NoPublishPlugin)
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

lazy val docs = project.in(file("docs"))
  .settings(
    commonSettings,
    releaseSettings,
    micrositeSettings,
    publish / skip := true,
    githubWorkflowArtifactUpload := false
  )
  .dependsOn(coreJVM)
  .enablePlugins(MicrositesPlugin)
  .enablePlugins(TutPlugin)

val catsV = "2.3.1"
val catsEffectV = "2.3.1"
val uniqueV = "2.1.0-M10"
val disciplineSpecs2V = "1.1.3"
val specs2V = "4.5.1"

// General Settings
lazy val commonSettings = Seq(
  organization := "io.chrisdavenport",
  libraryDependencies ++= Seq(
    "org.typelevel"               %%% "cats-core"                  % catsV,
    "org.typelevel"               %%% "cats-effect"                % catsEffectV,
    "org.typelevel"               %%% "unique"                     % uniqueV,
    "org.typelevel"               %%% "cats-laws"                  % catsV              % Test,
    "org.typelevel"               %%% "discipline-specs2"          % disciplineSpecs2V  % Test,
  ),
  // As of 3.0.0-M3, it's still broken
  useScala3doc := false
)

lazy val releaseSettings = {
  Seq(
    publishArtifact in Test := false,
    scmInfo := Some(
      ScmInfo(
        url("https://github.com/typelevel/vault"),
        "git@github.com:typelevel/vault.git"
      )
    ),
    homepage := Some(url("https://github.com/typelevel/vault")),
    licenses := List("MIT" -> url("http://opensource.org/licenses/MIT")),
  )
}

lazy val micrositeSettings = {
  import microsites._
  Seq(
    micrositeName := "vault",
    micrositeDescription := "Type-safe, persistent storage for values of arbitrary types",
    micrositeAuthor := "Typelevel",
    micrositeGithubOwner := "typelevel",
    micrositeGithubRepo := "vault",
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
    fork in tut := true,
    scalacOptions in Tut --= Seq(
      "-Xfatal-warnings",
      "-Ywarn-unused-import",
      "-Ywarn-numeric-widen",
      "-Ywarn-dead-code",
      "-Ywarn-unused:imports",
      "-Xlint:-missing-interpolator,_"
    ),
    libraryDependencies += "com.47deg" %% "github4s" % "0.27.1",
    micrositePushSiteWith := GitHub4s,
    micrositeGithubToken := sys.env.get("GITHUB_TOKEN"),
    micrositeExtraMdFiles := Map(
      file("CHANGELOG.md")        -> ExtraMdFileConfig("changelog.md", "page", Map("title" -> "changelog", "section" -> "changelog", "position" -> "100")),
      file("CODE_OF_CONDUCT.md")  -> ExtraMdFileConfig("code-of-conduct.md",   "page", Map("title" -> "code of conduct",   "section" -> "code of conduct",   "position" -> "101")),
      file("LICENSE")             -> ExtraMdFileConfig("license.md",   "page", Map("title" -> "license",   "section" -> "license",   "position" -> "102"))
    )
  )
}
