package cf.proxy

import akka.actor.{Actor, ActorLogging, Props}
import com.typesafe.config.Config
import spray.can.Http

class ProxyListener(conf: Config) extends Actor with ActorLogging {

  log.info("* * * * * ProxyListener Start......")

  implicit val system = this.context.system

  override def receive: Receive = {
    case Http.Connected(remote, _) =>
      log.debug(s"connect from ${remote.getAddress}:${remote.getPort}")
      val stub = system.actorOf(Props(classOf[ProxyConnection], conf))
      sender ! Http.Register(stub)
    case m => log.error("Unknown: " + m)
  }
}
