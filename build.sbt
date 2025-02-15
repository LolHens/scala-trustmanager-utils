lazy val scalaVersions = Seq("3.3.5", "2.13.16", "2.12.20")

ThisBuild / scalaVersion := scalaVersions.head
ThisBuild / versionScheme := Some("early-semver")
ThisBuild / organization := "de.lhns"
name := (core.projectRefs.head / name).value

val V = new {
  val betterMonadicFor = "0.3.1"
  val cats = "2.13.0"
  val log4s = "1.10.0"
  val logbackClassic = "1.5.16"
  val munit = "1.1.0"
  val munitTaglessFinal = "0.3.0"
  val slf4j = "2.0.16"
}

lazy val commonSettings: SettingsDefinition = Def.settings(
  version := {
    val Tag = "refs/tags/v?([0-9]+(?:\\.[0-9]+)+(?:[+-].*)?)".r
    sys.env.get("CI_VERSION").collect { case Tag(tag) => tag }
      .getOrElse("0.0.1-SNAPSHOT")
  },

  licenses += ("Apache-2.0", url("https://www.apache.org/licenses/LICENSE-2.0")),

  homepage := scmInfo.value.map(_.browseUrl),
  scmInfo := Some(
    ScmInfo(
      url("https://github.com/lhns/scala-trustmanager-utils"),
      "scm:git@github.com:lhns/scala-trustmanager-utils.git"
    )
  ),
  developers := List(
    Developer(id = "lhns", name = "Pierre Kisters", email = "pierrekisters@gmail.com", url = url("https://github.com/lhns/"))
  ),

  libraryDependencies ++= Seq(
    "ch.qos.logback" % "logback-classic" % V.logbackClassic % Test,
    "de.lhns" %% "munit-tagless-final" % V.munitTaglessFinal % Test,
    "org.scalameta" %% "munit" % V.munit % Test,
  ),

  testFrameworks += new TestFramework("munit.Framework"),

  libraryDependencies ++= virtualAxes.?.value.getOrElse(Seq.empty).collectFirst {
    case VirtualAxis.ScalaVersionAxis(version, _) if version.startsWith("2.") =>
      compilerPlugin("com.olegpy" %% "better-monadic-for" % V.betterMonadicFor)
  },

  Compile / doc / sources := Seq.empty,

  publishMavenStyle := true,

  publishTo := sonatypePublishToBundle.value,

  sonatypeCredentialHost := {
    if (sonatypeProfileName.value == "de.lolhens")
      "oss.sonatype.org"
    else
      "s01.oss.sonatype.org"
  },

  credentials ++= (for {
    username <- sys.env.get("SONATYPE_USERNAME")
    password <- sys.env.get("SONATYPE_PASSWORD")
  } yield Credentials(
    "Sonatype Nexus Repository Manager",
    sonatypeCredentialHost.value,
    username,
    password
  )).toList,

  pomExtra := {
    if (sonatypeProfileName.value == "de.lolhens")
      <distributionManagement>
        <relocation>
          <groupId>de.lhns</groupId>
        </relocation>
      </distributionManagement>
    else
      pomExtra.value
  }
)

lazy val root: Project =
  project
    .in(file("."))
    .settings(commonSettings)
    .settings(
      publishArtifact := false,
      publish / skip := true
    )
    .aggregate(core.projectRefs: _*)

lazy val core = projectMatrix.in(file("core"))
  .settings(commonSettings)
  .settings(
    name := "scala-trustmanager-utils",

    libraryDependencies ++= Seq(
      "org.log4s" %% "log4s" % V.log4s,
      "org.slf4j" % "slf4j-api" % V.slf4j,
      "org.typelevel" %% "cats-core" % V.cats,
    ),
  )
  .jvmPlatform(scalaVersions)
