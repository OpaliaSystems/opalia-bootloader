package systems.opalia.bootloader

import com.typesafe.config.{Config, ConfigFactory, ConfigParseOptions, ConfigResolveOptions}
import java.nio.file.Paths
import systems.opalia.commons.application.AbstractApplication


abstract class BootloaderApplication()
  extends AbstractApplication(Paths.get("./application.pid")) {

  val config: Config = loadConfig()
  val bootloader: Bootloader = modifyBootloaderBuilder(BootloaderBuilder.newBootloaderBuilder(config)).newBootloader()

  protected def loadConfig(): Config = {

    val basePath = Paths.get("./tmp")

    ConfigFactory.load(
      ConfigParseOptions.defaults(),
      ConfigResolveOptions.defaults()
        .setAllowUnresolved(true)
        .setUseSystemEnvironment(true)
    )
      .resolveWith(ConfigFactory.parseString(s"""base-path = $basePath"""))
      .resolve()
  }

  protected def modifyBootloaderBuilder(bootBuilder: BootloaderBuilder): BootloaderBuilder = {

    bootBuilder.withShutdownHookFlag(true)
  }
}
