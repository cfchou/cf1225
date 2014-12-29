package cf

import akka.actor.{ActorLogging, Props, Actor}
import akka.actor.Actor.Receive
import akka.pattern.{ask, pipe}
import akka.util.Timeout
import com.typesafe.config.Config
import spray.can.Http
import spray.http.{HttpResponse, HttpMethods, HttpRequest}
import scala.concurrent.ExecutionContext
import scala.concurrent.duration._

class StubHandler(conf:Config) extends Actor with ActorLogging {

  implicit val system = this.context.system

  implicit val ec = this.context.dispatcher

  implicit val reqTimeout = {
    val dura = Duration(conf.getString("spray.can.server.request-timeout"))
    log.debug(s"reqest-timeout: ${dura.length} ${dura.unit}")
    Timeout.durationToTimeout(FiniteDuration(dura.length, dura.unit))
  }

  // TODO: test if generalizing to a remote actor works
  val handler = system.actorOf(Props[RealHandler])

  override def receive: Receive = {
    case m =>
      log.debug("Stub: " + m)
      handler ? m pipeTo(sender())
  }
}