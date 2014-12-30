package cf.proxy

import akka.actor.Actor.Receive
import akka.actor.{ActorLogging, Actor}
import spray.can.Http.HostConnectorSetup

class ProxyStubMx(targets: List[(String, Int)]) extends Actor
  with ActorLogging {

  implicit val system = this.context.system

  /*
  val mx = conf.getConfigList("gate.multiplexTo").map { c =>
    (c.getString("interface"), c.getInt("port"))
  }.toList

  lazy val connectors = {

    val setups = mx.map { c =>
      Http.HostConnectorSetup(host = c._1, port = c._2,
        connectionType = ClientConnectionType.Proxied(c._1, c._2))
    }

    val fs = setups.map { s =>
      val f = IO(Http).ask(s) map {
        case Http.HostConnectorInfo(connector, _) => List(connector)
      }
      f.recover { case _ => List.empty[ActorRef] }
    }

    Future.sequence(fs).map(ll => ll.flatten )
  }
  */

  override def receive: Receive = {
    case m =>
      log.debug(s"msg: $m")
  }
}
