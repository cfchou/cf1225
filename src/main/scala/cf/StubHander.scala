package cf

import akka.actor.Actor
import akka.actor.Actor.Receive
import grizzled.slf4j.Logger
import spray.can.Http
import spray.http.{HttpResponse, HttpMethods, HttpRequest}

class StubHander extends Actor {

  val log = Logger[this.type]

  implicit val system = this.context.system

  override def receive: Receive = {

    case HttpRequest(HttpMethods.GET, uri, headers, entity, _) =>
      log.debug(s"GET $uri")
      sender ! HttpResponse()
    case HttpRequest(method, uri, headers, entity, _) =>
      log.debug(s"$method $uri")
      sender ! HttpResponse()
    case m : Http.ConnectionClosed => log.info("ConnectionClosed: " + m)
    case m =>
      log.error("Unknown: " + m)
  }
}
