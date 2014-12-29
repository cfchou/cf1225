package cf.gate

import akka.actor.{ActorLogging, Props, Actor}
import akka.actor.Actor.Receive
import com.typesafe.config.Config
import spray.can.Http

class GateHandler(val conf: Config) extends Actor with ActorLogging {

  log.debug("GateHandler started......")

  implicit val system = this.context.system

  override def receive: Receive = {
    case Http.Connected(remote, _) =>
      log.debug(s"connect from ${remote.getAddress}:${remote.getPort}")
      val stub = system.actorOf(Props(classOf[StubHandler], conf))
      sender ! Http.Register(stub)
    case m => log.error("Unknown: " + m)
  }
}
