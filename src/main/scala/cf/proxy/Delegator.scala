package cf.proxy

import akka.actor.Actor.Receive
import akka.actor._
import akka.io.IO
import akka.pattern.{ask, pipe}
import com.typesafe.config.Config
import spray.can.Http
import spray.can.Http.ClientConnectionType
import spray.http.{StatusCodes, HttpResponse, HttpRequest}

import scala.util.Success

class Delegator(target: (String, Int), conf: Config)
  extends Actor  with ActorLogging {

  log.info("* * * * * Delegator Start...")

  implicit val system = context.system
  implicit val ec = context.dispatcher
  implicit val connTimeout = cf.connectingTimeout(conf)

  val setup = Http.HostConnectorSetup(host = target._1, port = target._2,
    connectionType = ClientConnectionType.Proxied(target._1, target._2))

  IO(Http).ask(setup).onComplete {
    case Success(Http.HostConnectorInfo(c, _)) =>
      context.become(connected(c) orElse default)
    case e =>
      log.error("connect failed: " + e)
      // ActorKilledException triggers a failure. leave the supervisor to deal
      // with it
      self.tell(Kill.getInstance, ActorRef.noSender)
  }

  override def receive: Receive = waiting orElse default

  def waiting: Receive = {
    case m@HttpRequest(method, uri, _, _, _) =>
      log.debug(s"not ready for $method $uri")
      sender ! HttpResponse(status = StatusCodes.ServiceUnavailable)
  }

  def connected(major: ActorRef): Receive = {
    case m: HttpRequest =>
      log.debug(s"Delegator of ${target._1}:${target._2}: " + m)
      major ? m pipeTo(sender())
  }

  def default: Receive = {
    case m : Http.ConnectionClosed =>
      log.info("ConnectionClosed: " + m)
      self ! PoisonPill
      // FIXME: tell the supervisor by throwing a custom exception?
    case m => log.error("Unknown: " + m)
  }
}
