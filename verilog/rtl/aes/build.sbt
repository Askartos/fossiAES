//
def scalacOptionsVersion(scalaVersion: String): Seq[String] = {
  Seq() ++ {
    CrossVersion.partialVersion(scalaVersion) match {
      case Some((2, scalaMajor: Long)) if scalaMajor < 12 => Seq()
      case _ => Seq("-Xsource:2.11")
    }
  }
}

def javacOptionsVersion(scalaVersion: String): Seq[String] = {
  Seq() ++ {
      CrossVersion.partialVersion(scalaVersion) match {
      case Some((2, scalaMajor: Long)) if scalaMajor < 12 =>
        Seq("-source", "1.7", "-target", "1.7")
      case _ =>
        Seq("-source", "1.8", "-target", "1.8")
    }
  }
}

name := "arcabuco"

version := "1.0"


mainClass in (Compile, packageBin) := Some("fossiAES.aesMain")
mainClass in (Compile, run) := Some("fossiAES.aesMain")

scalaVersion := "2.12.6"
crossScalaVersions := Seq("2.12.6", "2.11.12")

resolvers ++= Seq(
  Resolver.sonatypeRepo("snapshots"),
  Resolver.sonatypeRepo("releases")
)

val defaultVersions = Map("chisel3" -> "3.4.+")

addCompilerPlugin("org.scalamacros" % "paradise" % "2.1.0" cross CrossVersion.full)

libraryDependencies ++= Seq("chisel3").map {  dep: String => "edu.berkeley.cs" %% dep % sys.props.getOrElse(dep + "Version", defaultVersions(dep)) }

scalacOptions ++= scalacOptionsVersion(scalaVersion.value)
scalacOptions ++= Seq("-language:reflectiveCalls")
scalacOptions ++= Seq("-language:implicitConversions")
scalacOptions ++= Seq("-unchecked", "-deprecation","-feature")
javacOptions 	++= javacOptionsVersion(scalaVersion.value)
