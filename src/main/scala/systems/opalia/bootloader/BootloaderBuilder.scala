package systems.opalia.bootloader

import com.typesafe.config.Config
import java.net.JarURLConnection
import java.nio.file.{Path, Paths}
import org.osgi.framework.Constants
import scala.collection.JavaConverters._
import scala.collection.immutable.ListMap
import systems.opalia.commons.configuration.ConfigHelper._
import systems.opalia.commons.configuration.Reader._


final class BootloaderBuilder private(val bundleConfig: Config,
                                      val cacheDirectory: Path,
                                      val bootDelegations: Seq[String],
                                      val extraExportPackages: Seq[String],
                                      val remoteRepositories: ListMap[String, String],
                                      val localRepository: String,
                                      val bundleArtifacts: Seq[String],
                                      val useShutdownHook: Boolean) {

  def withCacheDirectory(cacheDirectory: Path): BootloaderBuilder = {

    new BootloaderBuilder(
      bundleConfig,
      cacheDirectory.toAbsolutePath.normalize,
      bootDelegations,
      extraExportPackages,
      remoteRepositories,
      localRepository,
      bundleArtifacts,
      useShutdownHook
    )
  }

  def withBootDelegations(packageNames: Seq[String]): BootloaderBuilder = {

    new BootloaderBuilder(
      bundleConfig,
      cacheDirectory,
      packageNames.distinct,
      extraExportPackages,
      remoteRepositories,
      localRepository,
      bundleArtifacts,
      useShutdownHook
    )
  }

  def withBootDelegation(packageName: String): BootloaderBuilder = {

    new BootloaderBuilder(
      bundleConfig,
      cacheDirectory,
      (bootDelegations :+ packageName).distinct,
      extraExportPackages,
      remoteRepositories,
      localRepository,
      bundleArtifacts,
      useShutdownHook
    )
  }

  def withoutBootDelegation(packageName: String): BootloaderBuilder = {

    new BootloaderBuilder(
      bundleConfig,
      cacheDirectory,
      bootDelegations.filterNot(_ == packageName),
      extraExportPackages,
      remoteRepositories,
      localRepository,
      bundleArtifacts,
      useShutdownHook
    )
  }

  def withExtraExportPackages(packageNames: Seq[String]): BootloaderBuilder = {

    new BootloaderBuilder(
      bundleConfig,
      cacheDirectory,
      bootDelegations,
      packageNames.distinct,
      remoteRepositories,
      localRepository,
      bundleArtifacts,
      useShutdownHook
    )
  }

  def withExtraExportPackage(packageName: String): BootloaderBuilder = {

    new BootloaderBuilder(
      bundleConfig,
      cacheDirectory,
      bootDelegations,
      (extraExportPackages :+ packageName).distinct,
      remoteRepositories,
      localRepository,
      bundleArtifacts,
      useShutdownHook
    )
  }

  def withoutExtraExportPackage(packageName: String): BootloaderBuilder = {

    new BootloaderBuilder(
      bundleConfig,
      cacheDirectory,
      bootDelegations,
      extraExportPackages.filterNot(_ == packageName),
      remoteRepositories,
      localRepository,
      bundleArtifacts,
      useShutdownHook
    )
  }

  def withRemoteRepositories(idUriMap: ListMap[String, String]): BootloaderBuilder = {

    new BootloaderBuilder(
      bundleConfig,
      cacheDirectory,
      bootDelegations,
      extraExportPackages,
      idUriMap,
      localRepository,
      bundleArtifacts,
      useShutdownHook
    )
  }

  def withRemoteRepository(id: String, uri: String): BootloaderBuilder = {

    new BootloaderBuilder(
      bundleConfig,
      cacheDirectory,
      bootDelegations,
      extraExportPackages,
      remoteRepositories + (id -> uri),
      localRepository,
      bundleArtifacts,
      useShutdownHook
    )
  }

  def withoutRemoteRepository(id: String): BootloaderBuilder = {

    new BootloaderBuilder(
      bundleConfig,
      cacheDirectory,
      bootDelegations,
      extraExportPackages,
      remoteRepositories - id,
      localRepository,
      bundleArtifacts,
      useShutdownHook
    )
  }

  def withLocalRepository(uri: String): BootloaderBuilder = {

    new BootloaderBuilder(
      bundleConfig,
      cacheDirectory,
      bootDelegations,
      extraExportPackages,
      remoteRepositories,
      uri,
      bundleArtifacts,
      useShutdownHook
    )
  }

  def withBundles(bundleArtifacts: Seq[String]): BootloaderBuilder = {

    new BootloaderBuilder(
      bundleConfig,
      cacheDirectory,
      bootDelegations,
      extraExportPackages,
      remoteRepositories,
      localRepository,
      bundleArtifacts.distinct,
      useShutdownHook
    )
  }

  def withBundle(bundleArtifact: String): BootloaderBuilder = {

    new BootloaderBuilder(
      bundleConfig,
      cacheDirectory,
      bootDelegations,
      extraExportPackages,
      remoteRepositories,
      localRepository,
      (bundleArtifacts :+ bundleArtifact).distinct,
      useShutdownHook
    )
  }

  def withoutBundle(bundleArtifact: String): BootloaderBuilder = {

    new BootloaderBuilder(
      bundleConfig,
      cacheDirectory,
      bootDelegations,
      extraExportPackages,
      remoteRepositories,
      localRepository,
      bundleArtifacts.filterNot(_ == bundleArtifact),
      useShutdownHook
    )
  }

  def withShutdownHookFlag(flag: Boolean): BootloaderBuilder = {

    new BootloaderBuilder(
      bundleConfig,
      cacheDirectory,
      bootDelegations,
      extraExportPackages,
      remoteRepositories,
      localRepository,
      bundleArtifacts,
      flag
    )
  }

  def newBootloader(): Bootloader = {

    val exports =
      extraExportPackages
        .flatMap(listPackages)
        .distinct
        .map {
          case (packageName, properties) =>

            val version =
              (properties.get("Bundle-Version"), properties.get("Specification-Version")) match {
                case (Some(x), _) => x
                case (None, Some(x)) => x
                case _ => throw new IllegalArgumentException(s"Cannot get version of package “$packageName”.")
              }

            s"""$packageName;version="${version.takeWhile(_ != '-')}""""
        }

    val frameworkConfig =
      Map(
        Constants.FRAMEWORK_STORAGE -> cacheDirectory.toString,
        Constants.FRAMEWORK_STORAGE_CLEAN -> Constants.FRAMEWORK_STORAGE_CLEAN_ONFIRSTINIT,
        Constants.FRAMEWORK_BOOTDELEGATION -> bootDelegations.mkString(","),
        Constants.FRAMEWORK_SYSTEMPACKAGES_EXTRA -> exports.mkString(",")
      )

    val resolver = new ArtifactResolver(remoteRepositories, localRepository)

    new Bootloader(
      frameworkConfig,
      bundleConfig,
      useShutdownHook,
      bundleArtifacts.map(resolver.resolve)
    )
  }

  private def listPackages(packageName: String): Seq[(String, Map[String, String])] = {

    def process(packageName: String): Seq[(String, Map[String, String])] =
      this.getClass.getClassLoader.getResources(packageName.replace('.', '/'))
        .asScala
        .toSeq
        .flatMap {
          uri =>

            if (uri.getProtocol == "jar") {

              val jarConnection = uri.openConnection.asInstanceOf[JarURLConnection]
              val jarFile = jarConnection.getJarFile
              val attributes = jarFile.getManifest.getMainAttributes
              val entries = jarFile.entries.asScala

              entries
                .filter(x => x.getName.startsWith(jarConnection.getEntryName + "/") && x.getName.endsWith("/"))
                .map(x => (x.getName.dropRight(1).replace('/', '.'), attributes))
                .map(x => (x._1, x._2.asScala.toMap.map(x => (x._1.toString, x._2.toString))))

            } else
              throw new IllegalArgumentException(s"Cannot handle URI scheme ${uri.getProtocol}.")
        }

    if (packageName.endsWith(".*"))
      process(packageName.dropRight(2))
    else
      process(packageName).filter(_._1 == packageName)
  }
}

object BootloaderBuilder {

  def newBootloaderBuilder(bundleConfig: Config): BootloaderBuilder = {

    val cacheDirectory =
      bundleConfig.as[Option[Path]]("booting.cache-directory")
        .map(_.toAbsolutePath.normalize)
        .getOrElse(Paths.get("./tmp/felix-cache"))

    val bootDelegations =
      bundleConfig.as[Option[Seq[String]]]("booting.boot-delegations")
        .map(_.distinct)
        .getOrElse(Seq("javax.*", "sun.*", "com.sun.*", "org.xml.*", "org.w3c.*"))

    val extraExportPackages =
      bundleConfig.as[Option[Seq[String]]]("booting.extra-export-packages")
        .map(_.distinct)
        .getOrElse(Seq("scala.*", "com.typesafe.config.*", "systems.opalia.interfaces.*"))

    val remoteRepositories =
      bundleConfig.as[Option[Seq[Config]]]("booting.remote-repositories")
        .map(_.map {
          cnf =>

            val id = cnf.as[String]("id")
            val uri = cnf.as[String]("uri")

            id -> uri
        })
        .getOrElse {

          Seq(
            "public" -> "https://repo1.maven.org/maven2/",
            "jcenter" -> "https://jcenter.bintray.com/",
            "Sonatype OSS Releases" -> "https://oss.sonatype.org/content/repositories/releases/",
            "Sonatype OSS Snapshots" -> "https://oss.sonatype.org/content/repositories/snapshots/"
          )
        }

    val localRepository =
      bundleConfig.as[Option[String]]("booting.local-repository")
        .getOrElse(s"${System.getProperty("user.home")}/.m2/repository/")

    val bundleArtifacts =
      bundleConfig.as[Option[Seq[String]]]("booting.bundle-artifacts")
        .map(_.distinct)
        .getOrElse(Seq.empty)

    val useShutdownHook =
      bundleConfig.as[Option[Boolean]]("booting.use-shutdown-hook")
        .getOrElse(false)

    new BootloaderBuilder(
      bundleConfig,
      cacheDirectory,
      bootDelegations,
      extraExportPackages,
      ListMap(remoteRepositories: _*),
      localRepository,
      bundleArtifacts,
      useShutdownHook
    )
  }
}
