package akka.io.spdy

import akka.actor._
import com.typesafe.config.Config

object Spdy extends ExtensionKey[SpdyExt] with SpdyCommands {

}

trait SpdyCommands {

}

class SpdyExt(system: ExtendedActorSystem) extends akka.io.IO.Extension {

  val Settings = new Settings(system.settings.config getConfig "spdy")

  class Settings private[SpdyExt](config: Config) {
    val ManagerDispatcher = config getString "manager-dispatcher"
    val SettingsGroupDispatcher = config getString "settings-group-dispatcher"
    val HostConnectorDispatcher = config getString "host-connector-dispatcher"
    val ListenerDispatcher = config getString "listener-dispatcher"
    val ConnectionDispatcher = config getString "connection-dispatcher"
  }

  val manager = system.actorOf(
    props = Props(new SpdyManager(Settings)) withDispatcher Settings.ManagerDispatcher,
    name = "IO-HTTP"
  )
}
