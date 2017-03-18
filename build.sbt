val scala210Version = "2.10.6"
val scala211Version = "2.11.8"
val scala212Version = "2.12.1"

val disciplineVersion = "0.7.2"
val fastParseVersion = "0.4.2"
val scalaCheckVersion = "1.13.4"
val scalaTestVersion = "3.0.1"
val spireVersion = "0.13.1-SNAPSHOT"

lazy val unparsing = (project in file("."))
  .settings(moduleName := "unparsing")
  .settings(unparsingSettings: _*)
  .settings(noPublishSettings)
  .aggregate(core, spire, tests)
  .dependsOn(core, spire, tests)

lazy val core = (project in file("core"))
  .settings(moduleName := "unparsing-core")
  .settings(unparsingSettings: _*)
  .settings(commonJvmSettings: _*)

lazy val spire = (project in file("spire"))
  .settings(moduleName := "unparsing-spire")
  .settings(unparsingSettings: _*)
  .settings(testSettings: _*)
  .settings(spireSettings: _*)
  .settings(commonJvmSettings: _*)
  .dependsOn(core)

lazy val tests = (project in file("tests"))
  .settings(moduleName := "unparsing-tests")
  .settings(unparsingSettings: _*)
  .settings(testSettings:_*)
  .settings(noPublishSettings:_*)
  .settings(commonJvmSettings: _*)
  .dependsOn(core, spire)

lazy val unparsingSettings = buildSettings ++ commonSettings ++ publishSettings

lazy val buildSettings = Seq(
  organization := "net.alasc",
  scalaVersion := scala212Version,
  crossScalaVersions := Seq(scala211Version, scala210Version)
)

lazy val commonSettings = Seq(
  scalacOptions ++= commonScalacOptions.diff(Seq(
    "-Xfatal-warnings",
    "-Ywarn-dead-code",
    "-Ywarn-numeric-widen",
    "-Ywarn-value-discard"
  )),
  resolvers ++= Seq(
    Resolver.url("spirejars", url(file("spirejars").toURI.toASCIIString))(Resolver.ivyStylePatterns),
    "bintray/non" at "http://dl.bintray.com/non/maven",
    "bintray/denisrosset/maven" at "https://dl.bintray.com/denisrosset/maven",
    Resolver.sonatypeRepo("snapshots"),
    Resolver.sonatypeRepo("releases")
  )
) ++ warnUnusedImport

lazy val spireSettings = Seq(
  libraryDependencies ++= Seq(
    "org.spire-math" %% "spire" % spireVersion,
    "org.spire-math" %% "spire-laws" % spireVersion,
    "com.lihaoyi" %% "fastparse" % fastParseVersion
  )
)

lazy val publishSettings = Seq(
  homepage := Some(url("https://github.com/denisrosset/unparsing")),
  licenses += ("MIT", url("http://opensource.org/licenses/MIT")),
  pomExtra := (
    <scm>
      <url>git@github.com:denisrosset/unparsing.git</url>
      <connection>scm:git:git@github.com:denisrosset/unparsing.git</connection>
    </scm>
    <developers>
      <developer>
        <id>denisrosset</id>
        <name>Denis Rosset</name>
        <url>http://github.com/denisrosset/</url>
      </developer>
    </developers>
  ),
  bintrayRepository := "maven",
  publishArtifact in Test := false
)

lazy val noPublishSettings = Seq(
  publish := (),
  publishLocal := (),
  publishArtifact := false
)

lazy val commonScalacOptions = Seq(
  "-deprecation",
  "-encoding", "UTF-8",
  "-feature",
  "-language:existentials",
  "-language:higherKinds",
  "-language:implicitConversions",
  "-language:experimental.macros",
  "-unchecked",
  "-Xfatal-warnings",
  "-Xlint",
  "-Yno-adapted-args",
  "-Ywarn-dead-code",
  "-Ywarn-numeric-widen",
  "-Ywarn-value-discard",
  "-Xfuture"
)

lazy val commonJvmSettings = Seq(
  testOptions in Test += Tests.Argument(TestFrameworks.ScalaTest, "-oDF")
) ++ selectiveOptimize
  // -optimize has no effect in scala-js other than slowing down the build


// do not optimize on Scala 2.10 because of optimizer bugs (cargo-cult setting
// from my experience with metal)
lazy val selectiveOptimize = 
  scalacOptions ++= {
    CrossVersion.partialVersion(scalaVersion.value) match {
      case Some((2, 10)) => Seq()
      case Some((2, 11)) => Seq("-optimize")
      case Some((2, 12)) => Seq()
      case _ => sys.error("Unknown Scala version")
    }
  }

lazy val warnUnusedImport = Seq(
  scalacOptions ++= {
    CrossVersion.partialVersion(scalaVersion.value) match {
      case Some((2, 10)) =>
        Seq()
      case Some((2, n)) if n >= 11 =>
        Seq("-Ywarn-unused-import")
    }
  },
  scalacOptions in (Compile, console) ~= {_.filterNot("-Ywarn-unused-import" == _)},
  scalacOptions in (Test, console) := (scalacOptions in (Compile, console)).value
)

lazy val testSettings = Seq(
  libraryDependencies ++= Seq(
    "org.scalatest" %% "scalatest" % scalaTestVersion,
    "org.typelevel" %% "discipline" % disciplineVersion,
    "org.scalacheck" %% "scalacheck" % scalaCheckVersion
  )
)
