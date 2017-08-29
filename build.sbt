import Dependencies._

lazy val root = (project in file(".")).
  settings(
    inThisBuild(List(
      organization := "lu.intech",
      scalaVersion := "2.12.3",
      version      := "0.1.0-SNAPSHOT"
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