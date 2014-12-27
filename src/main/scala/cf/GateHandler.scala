package cf

import akka.actor.Actor
import akka.actor.Actor.Receive
import com.typesafe.config.Config
import spray.http.{HttpResponse, HttpMethods, HttpRequest}
import grizzled.slf4j.Logger

class GateHandler(val conf: Config) extends Actor {

  val log = Logger[this.type]

  log.debug("GateHandler started......")

  override def receive: Receive = {
    case HttpRequest(HttpMethods.GET, uri, headers, entity, _) =>
      log.debug(s"GET $uri")
      sender ! HttpResponse()
    case HttpRequest(method, uri, headers, entity, _) =>
      log.debug(s"$method $uri")
      sender ! HttpResponse()
    case m =>
      log.error("Not a HttpRequest: " + m)
  }
}
