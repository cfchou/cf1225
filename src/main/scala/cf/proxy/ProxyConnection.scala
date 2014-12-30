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

class ProxyConnection(conf: Config) extends Actor with ActorLogging {

  log.info("* * * * * ProxyConnection Start......")

  implicit val system = this.context.system
  implicit val ec = context.dispatcher
  implicit val connTimeout = cf.connectingTimeout(conf)

  val mx = conf.getConfigList("gate.multiplexTo").map { c =>
    (c.getString("interface"), c.getInt("port"))
  }.toList

  val (major, rest) = if (mx.nonEmpty) {
    val a = Some(system.actorOf(Props(classOf[Delegator], mx.head, conf)))
    val b = if (mx.length > 1) {
      Some(system.actorOf(Props(classOf[DelegatorMx], mx.tail, conf)))
    } else {
      None
    }
    (a,b)
  } else {
    log.warning("empty targets")
    // stop would discard messages in the mailbox
    context.stop(self)
    (None, None)
  }

  override def receive: Receive = {
    case m@HttpRequest(method, uri, _, _, _) =>
      log.debug(s" $method $uri")
      rest foreach { _ ! m }
      major foreach {
        _ ? m pipeTo(sender())
      }
    case m : Http.ConnectionClosed =>
      log.info("ConnectionClosed: " + m)
      context.stop(self)
    case m => log.error("Unknown: " + m)
  }
}
