package cf.proxy

import akka.actor._
import akka.io.{IO, Tcp}
import com.typesafe.config.{Config, ConfigFactory}
import grizzled.slf4j.Logger
import spray.can.Http

object ProxyApp extends App {

  case object START
  case object STOP

  val log = Logger[this.type]

  implicit val system = ActorSystem("ProxyApp")

  val conf = ConfigFactory.load()

  val runner = system.actorOf(Props(classOf[ProxyApp], conf))

  log.info("Start...")

  runner ! START
  /*
  log.debug("Start...")
  val lf = List(Future.successful(1), Future.failed(new NoSuchElementException),
    Future.successful(2))
  val fl = Future.sequence(lf)
  fl.value.get match {
    case Success(v) => log.info("succ size: " + v.length)
    case Failure(e) => log.error(e)
  }
  log.debug("Stop...")
  */

  /*
  runAfter(5.seconds, system.scheduler) {

  }

  def runAfter[T](finiteDuration: FiniteDuration, using: Scheduler)(e: => T) = {
    val timeoutF = after(finiteDuration, system.scheduler)(Future(e))
  }
  */
}

class ProxyApp(conf: Config) extends Actor with ActorLogging {

  import cf.proxy.ProxyApp.{START, STOP}

  log.debug("ProxyApp Start...")

  implicit val system = this.context.system
  var listener: Option[ActorRef] = None

  lazy val start = {
    // TODO: multiple interfaces/ports
    val inf = conf.getString("gate.multiplexer.interface")

    val prt = conf.getInt("gate.multiplexer.port")

    val handler = system.actorOf(Props(classOf[ProxyHandler], conf))
    log.debug(s"Bind $inf:$prt")
    IO(Http) ! Http.Bind(handler, interface = inf, port = prt)
  }

  override def receive: Receive = {
    case START =>
      log.debug("START")
      start
    case STOP =>
      log.debug("STOP")
      listener.foreach(_ ! Http.Unbind)
    case m: Tcp.Bound =>
      log.debug(s"Bound: $m")
      listener = Some(listener.fold { sender() } { _ =>
        log.warning("listener exists, will be overwritten")
        sender()
      })
    case m: Tcp.Unbound =>
      log.debug(s"Unbound: $m")
      self ! PoisonPill
    case m: Http.CommandFailed =>
      log.error(s"Bind failed $m")
    case m =>
      log.error("Unknown: " + m)

  }
}

