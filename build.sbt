
organizationName := "Opalia Systems"

organizationHomepage := Some(url("https://opalia.systems"))

organization := "systems.opalia"

name := "bootloader"

description := "The project helps to boot the Opalia stack."

homepage := Some(url("https://github.com/OpaliaSystems/opalia-bootloader"))

version := "0.1.0"

scalaVersion := "2.12.8"

resolvers ++= Seq(
  sbt.librarymanagement.MavenRepository("Redhat GA", "https://maven.repository.redhat.com/ga/")
)

libraryDependencies ++= Seq(
  "systems.opalia" %% "interfaces" % "0.1.0-SNAPSHOT",
  "systems.opalia" %% "commons" % "0.1.0-SNAPSHOT",
  "org.slf4j" % "slf4j-simple" % "1.7.26",
  "org.apache.felix" % "org.apache.felix.framework" % "6.0.2",
  "org.apache.felix" % "org.apache.felix.main" % "6.0.2",
  "org.eclipse.aether" % "aether-api" % "1.1.0",
  "org.eclipse.aether" % "aether-spi" % "1.1.0",
  "org.eclipse.aether" % "aether-util" % "1.1.0",
  "org.eclipse.aether" % "aether-impl" % "1.1.0",
  "org.eclipse.aether" % "aether-connector-basic" % "1.1.0",
  "org.eclipse.aether" % "aether-transport-file" % "1.1.0",
  "org.eclipse.aether" % "aether-transport-http" % "1.1.0",
  "org.apache.maven" % "maven-aether-provider" % "3.3.9.redhat-2",
  "org.scalatest" %% "scalatest" % "3.0.7" % "test"
)
