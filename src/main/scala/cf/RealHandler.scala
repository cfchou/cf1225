package cf

import akka.actor.{PoisonPill, Actor}
import akka.actor.Actor.Receive
import spray.can.Http
import spray.http.{HttpMethods, HttpResponse, HttpRequest}
import grizzled.slf4j.Logger

class RealHandler extends Actor {

  val log = Logger[this.type]

  override def receive: Receive = {
    case HttpRequest(HttpMethods.GET, uri, headers, entity, _) =>
      log.debug(s"GET $uri")
      sender ! HttpResponse()
    case HttpRequest(method, uri, headers, entity, _) =>
      log.debug(s"$method $uri")
      sender ! HttpResponse()
    case m : Http.ConnectionClosed =>
      log.info("ConnectionClosed: " + m)
      self ! PoisonPill
    case m => log.error("Unknown: " + m)
  }
}
