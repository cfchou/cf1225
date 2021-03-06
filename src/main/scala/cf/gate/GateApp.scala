package cf.gate

import java.io.BufferedReader

import akka.actor.Actor.Receive
import akka.actor._
import akka.io.IO
import akka.io.Tcp
import com.typesafe.config.{Config, ConfigFactory}
import grizzled.slf4j.Logger
import spray.can.Http
import scala.concurrent.duration._
import scala.concurrent.{Await, Promise, Future}
import scala.io.Source

import scala.util.Try

object GateApp extends App {

  case object START
  case object STOP

  val log = Logger[this.type]

  implicit val system = ActorSystem("GateApp")

  val conf = ConfigFactory.load()

  val runner = system.actorOf(Props(classOf[GateApp], conf))

  log.info("Start...")

  runner ! START

  val reader = Source.stdin.bufferedReader
  monitor(reader)
  reader.close()
  log.info("Stop...")
  Try { Await.ready(Promise[Unit].future, 5.seconds) }
  log.info("Shutdown...")
  system.shutdown();
  Try { Await.ready(Promise[Unit].future, 5.seconds) }

  def monitor(input: BufferedReader): Unit = {
    try {
      input.readLine() match {
        case m: String if "stop" == m.toLowerCase()=> runner ! STOP
        case m =>
          log.debug(s"Unknown cmd: $m")
          monitor(input)
      }
    } catch {
      case e: Exception => log.error(e)
    }
  }
}

class GateApp(conf: Config) extends Actor with ActorLogging {

  import GateApp.{START, STOP}

  log.info("* * * * * GateApp Start...")

  implicit val system = this.context.system
  var listener: Option[ActorRef] = None

  lazy val start = {
    // TODO: multiple interfaces/ports
    val inf = conf.getString("gate.interface")

    val prt = conf.getInt("gate.port")

    val handler = system.actorOf(Props(classOf[GateHandler], conf))
    log.info(s"Bind $inf:$prt")
    IO(Http) ! Http.Bind(handler, interface = inf, port = prt)
  }

  override def receive: Receive = {
    case START =>
      log.info("START")
      start
    case STOP =>
      log.info("STOP")
      listener.foreach(_ ! Http.Unbind)
    case m: Tcp.Bound =>
      log.info(s"Bound: $m")
      listener = Some(listener.fold { sender() } { _ =>
        log.warning("listener exists, will be overwritten")
        sender()
      })
    case m: Tcp.Unbound =>
      log.info(s"Unbound: $m")
      context.stop(self)
    case m: Http.CommandFailed =>
      log.error(s"Bind failed $m")
    case m =>
      log.error("Unknown: " + m)
  }
}
