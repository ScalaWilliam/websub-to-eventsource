package controllers

import javax.inject._

import akka.agent.Agent

import concurrent.duration._
import akka.stream.scaladsl.Source
import org.jsoup.Jsoup
import play.api.libs.EventSource.Event
import play.api.libs.iteratee.Concurrent
import play.api.libs.iteratee.streams.IterateeStreams
import play.api.libs.ws.WSClient
import play.api.mvc.{InjectedController, Request}

import scala.async.Async._
import scala.concurrent.ExecutionContext

//noinspection TypeAnnotation
@Singleton
class Main @Inject()(ws: WSClient)(implicit executionContext: ExecutionContext)
    extends InjectedController {

  private val subscribed = Agent(Set.empty[String])

  private val (broadcaster, pushHere) = Concurrent.broadcast[String]

  private val source =
    Source.fromPublisher(IterateeStreams.enumeratorToPublisher(broadcaster))

  def index = TODO

  def push(to: String) = Action.apply { req: Request[_] =>
    val topic = req.queryString("hub.topic").headOption
    val challenge = req.queryString("hub.challenge").headOption
    val leaseSeconds =
      req.queryString("hub.lease_seconds").map(_.toInt).headOption

    (topic, challenge, leaseSeconds) match {
      case (Some(t), Some(c), Some(l)) =>
        subscribed.send(_ + t)
        Ok(c)
      case _ =>
        pushHere.push(to)
        Ok("")
    }
  }

  def events(from: String) = Action.async {

    val document = Jsoup.parse(from)
    val hub = document.select("link[rel='hub']").first().attr("href")
    val self = document.select("link[rel='self']").first().attr("href")

    async {
      await {
        ws.url(hub)
          .post(
            Map(
              "hub.mode" -> Seq("subscribe"),
              "hub.topic" -> Seq(self),
              "hub.callback" -> Seq(
                s"https://websub-to-eventsource.herokuapp.com/push?to=${from}")
            )
          )
      }
      Ok.chunked(
        source
          .filter(_.contentEquals(from))
          .map(e => Event(e))
          .merge(Main.keepAlive))
    }

  }

}

object Main {
  val keepAlive = Source.tick(0.seconds, 10.seconds, Event(""))
}
