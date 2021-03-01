
organizationName := "Opalia Systems"

organizationHomepage := Some(url("https://opalia.systems"))

organization := "systems.opalia"

name := "bootloader"

description := "The project helps to boot the Opalia stack."

homepage := Some(url("https://github.com/OpaliaSystems/opalia-bootloader"))

version := "0.1.0"

scalaVersion := "2.12.13"

libraryDependencies ++= Seq(
  "systems.opalia" %% "interfaces" % "1.0.0",
  "systems.opalia" %% "commons" % "1.0.0",
  "org.slf4j" % "slf4j-simple" % "1.7.30",
  "org.apache.felix" % "org.apache.felix.framework" % "7.0.0",
  "org.apache.felix" % "org.apache.felix.main" % "7.0.0",
  "org.apache.maven.resolver" % "maven-resolver-api" % "1.6.1",
  "org.apache.maven.resolver" % "maven-resolver-spi" % "1.6.1",
  "org.apache.maven.resolver" % "maven-resolver-util" % "1.6.1",
  "org.apache.maven.resolver" % "maven-resolver-impl" % "1.6.1",
  "org.apache.maven.resolver" % "maven-resolver-connector-basic" % "1.6.1",
  "org.apache.maven.resolver" % "maven-resolver-transport-file" % "1.6.1",
  "org.apache.maven.resolver" % "maven-resolver-transport-http" % "1.6.1",
  "org.apache.maven" % "maven-resolver-provider" % "3.6.3",
  "org.scalatest" %% "scalatest" % "3.2.5" % "test"
)
