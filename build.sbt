
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
  "org.slf4j" % "slf4j-simple" % "1.7.26",
  "org.apache.felix" % "org.apache.felix.framework" % "6.0.2",
  "org.apache.felix" % "org.apache.felix.main" % "6.0.2",
  "org.apache.maven.resolver" % "maven-resolver-api" % "1.4.1",
  "org.apache.maven.resolver" % "maven-resolver-spi" % "1.4.1",
  "org.apache.maven.resolver" % "maven-resolver-util" % "1.4.1",
  "org.apache.maven.resolver" % "maven-resolver-impl" % "1.4.1",
  "org.apache.maven.resolver" % "maven-resolver-connector-basic" % "1.4.1",
  "org.apache.maven.resolver" % "maven-resolver-transport-file" % "1.4.1",
  "org.apache.maven.resolver" % "maven-resolver-transport-http" % "1.4.1",
  "org.apache.maven" % "maven-resolver-provider" % "3.6.2",
  "org.scalatest" %% "scalatest" % "3.0.7" % "test"
)
