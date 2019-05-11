package systems.opalia.bootloader

import com.typesafe.config.Config
import java.util.ServiceLoader
import org.eclipse.aether.artifact.Artifact
import org.osgi.framework.BundleContext
import org.osgi.framework.launch.{Framework, FrameworkFactory}
import scala.collection.JavaConverters._
import scala.concurrent.Await
import scala.concurrent.duration.Duration
import systems.opalia.interfaces.soa.osgi.ServiceManager
import systems.opalia.interfaces.soa.{Bootable, ConfigurationService}


final class Bootloader(frameworkConfig: Map[String, String],
                       bundleConfig: Config,
                       useShutdownHook: Boolean,
                       bundleArtifacts: Seq[Artifact])
  extends Bootable[Unit, Unit] {

  private val framework = getFramework(frameworkConfig)

  framework.start()

  val bundleContext: BundleContext = framework.getBundleContext

  private val serviceManager = new ServiceManager()
  private val bundles = bundleArtifacts.map(x => bundleContext.installBundle(s"file://${x.getFile.getAbsolutePath}"))

  def setupTask(): Unit = {

    if (useShutdownHook)
      sys.addShutdownHook({

        shutdown()
        Await.result(this.awaitDown(), Duration.Inf)
      })

    val configurationService =
      new ConfigurationService {

        def getConfiguration: Config =
          bundleConfig
      }

    serviceManager.registerService(bundleContext, classOf[ConfigurationService], configurationService)

    bundles.foreach(_.start())
  }

  def shutdownTask(): Unit =
    synchronized {

      bundles.reverse.foreach(_.stop())

      serviceManager.unregisterServices()
      serviceManager.ungetServices(bundleContext)

      framework.stop()
      framework.waitForStop(0)
    }

  private def getFramework(frameworkConfig: Map[String, String]): Framework = {

    val serviceLoader = ServiceLoader.load(classOf[FrameworkFactory])
    val frameworkFactory = serviceLoader.iterator().next()

    frameworkFactory.newFramework(frameworkConfig.asJava)
  }
}
