package cf

import akka.actor.{Props, Actor}
import akka.actor.Actor.Receive
import com.typesafe.config.Config
import spray.can.Http
import spray.http.{HttpResponse, HttpMethods, HttpRequest}
import grizzled.slf4j.Logger

class GateHandler(val conf: Config) extends Actor {

  val log = Logger[this.type]

  log.debug("GateHandler started......")

  implicit val system = this.context.system

  override def receive: Receive = {
    case Http.Connected(remote, _) =>
      log.debug(s"connect from ${remote.getAddress}:${remote.getPort}")
      val stub = system.actorOf(Props[StubHander])
      sender ! Http.Register(stub)
    case m =>
      log.error("Unknown: " + m)
  }
}
