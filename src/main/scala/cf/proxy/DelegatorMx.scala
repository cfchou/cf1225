package cf.proxy

import akka.actor.{Props, ActorLogging, Actor}
import com.typesafe.config.Config

class DelegatorMx(targets: List[(String, Int)], conf: Config) extends Actor
  with ActorLogging {

  log.info("* * * * * DelegatorMx Start...")

  implicit val system = this.context.system

  val refs = targets map { t =>
    system.actorOf(Props(classOf[Delegator], t, conf))
  }

  if (refs.isEmpty) {
    log.warning("empty targets")
    context.stop(self)
  }

  // fire'n go
  override def receive: Receive = {
    case m =>
      log.debug(s"msg: $m dispatch to ${refs.length} Delegators")
      refs map { _ ! m}
  }
}
