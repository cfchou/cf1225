package cf.proxy

import akka.actor.Actor.Receive
import akka.actor._
import akka.io.IO
import akka.pattern.{ask, pipe}
import akka.util.Timeout
import com.typesafe.config.Config
import spray.can.Http
import spray.can.Http.ClientConnectionType
import spray.http.{StatusCodes, HttpResponse, HttpRequest}
import scala.collection.JavaConversions._
//import scala.concurrent.ExecutionContext.Implicits.global

import scala.concurrent.duration._
import scala.util.{Failure, Success}

class ProxyStub(conf: Config) extends Actor with ActorLogging {

  implicit val system = this.context.system
  implicit val ec = this.context.dispatcher
  implicit val connTimeout = cf.connectingTimeout(conf)

  val (fst, others) = {
    val mx = conf.getConfigList("gate.multiplexTo").map { c =>
      (c.getString("interface"), c.getInt("port"))
    }.toList
    (mx.head, mx.tail)
  }

  val setup = Http.HostConnectorSetup(host = fst._1, port = fst._2,
    connectionType = ClientConnectionType.Proxied(fst._1, fst._2))

  IO(Http).ask(setup).onComplete {
    case Success(Http.HostConnectorInfo(c, _)) =>
      context.become(connected(c) orElse default)
    case e =>
      log.error("connect failed: " + e)
      self ! PoisonPill
  }

  val rest = system.actorOf(Props(classOf[ProxyStubMx], others))

  override def receive: Receive = waiting orElse default

  def waiting: Receive = {
    case m@HttpRequest(method, uri, _, _, _) =>
      log.debug(s"not ready for $method $uri")
      rest ! m
      sender ! HttpResponse(status = StatusCodes.ServiceUnavailable)
  }

  def connected(major: ActorRef): Receive = {
    case m =>
      log.debug("Stub: " + m)
      rest ! m
      major ? m pipeTo(sender())
  }

  def default: Receive = {
    case m : Http.ConnectionClosed =>
      log.info("ConnectionClosed: " + m)
      // FIXME

      self ! PoisonPill
    case m => log.error("Unknown: " + m)
  }

}
