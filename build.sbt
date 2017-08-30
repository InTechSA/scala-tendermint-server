import Dependencies._

lazy val root = (project in file(".")).
  settings(
    inThisBuild(List(
      organization := "lu.intech",
      scalaVersion := "2.12.3",
      version      := "1.0.0"
    )),
    name := "tendermint-server",
    libraryDependencies ++= Seq(
      akkaStream,
      grpcNetty,
      grpcScalapb,
      protobuf,
      slf4jApi,
      logbackClassic
    )
  )

PB.targets in Compile := Seq(
  scalapb.gen() -> (sourceManaged in Compile).value
)

pomIncludeRepository := { _ => false }



licenses := Seq("MIT" -> url("https://opensource.org/licenses/MIT"))

homepage := Some(url("https://github.com/InTechSA/scala-tendermint-server"))

scmInfo := Some(
  ScmInfo(
    url("https://github.com/InTechSA/scala-tendermint-server"),
    "scm:git@github.com:InTechSA/scala-tendermint-server.git"
  )
)

developers := List(
  Developer(
    id    = "antoined",
    name  = "Antoine Detante",
    email = "antoine.detante@intech.lu",
    url   = url("https://github.com/adetante")
  )
)

publishMavenStyle := true
publishArtifact in Test := false

useGpg := true

publishTo := {
  val nexus = "https://oss.sonatype.org/"
  if (isSnapshot.value)
    Some("snapshots" at nexus + "content/repositories/snapshots")
  else
    Some("releases"  at nexus + "service/local/staging/deploy/maven2")
}