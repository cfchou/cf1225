import akka.util.Timeout
import com.typesafe.config.{Config, ConfigFactory}

import scala.concurrent.duration.{FiniteDuration, Duration}

package object cf {

  def connectingTimeout(conf: Config) = {
    val dura = Duration(conf.getString("spray.can.client.connecting-timeout"))
    Timeout.durationToTimeout(FiniteDuration(dura.length, dura.unit))
  }

}
