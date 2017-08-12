package controllers

import java.net.URL
import javax.inject._

import akka.agent.Agent

import concurrent.duration._
import akka.stream.scaladsl.Source
import org.jsoup.Jsoup
import org.jsoup.nodes.Document
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

  def push(to: String) = Action {
    pushHere.push(to)
    Ok("")
  }

  def pushConfirm(to: String) = Action.apply { req: Request[_] =>
    val topic = req.queryString("hub.topic").headOption
    val challenge = req.queryString("hub.challenge").headOption
    val leaseSeconds =
      req.queryString("hub.lease_seconds").map(_.toInt).headOption

    (topic, challenge, leaseSeconds) match {
      case (Some(t), Some(c), Some(l)) =>
        subscribed.send(_ + t)
        Ok(c)
      case _ =>
        BadRequest("Cannot process this")
    }
  }

  def events(from: String) = Action.async { implicit r =>
    async {
      val firstUrl = await(ws.url(from).get())
      val hubHeader = firstUrl.headerValues("Link").collectFirst {
        case Main.matchLink(h, "hub") => h
      }
      val selfHeader = firstUrl.headerValues("Link").collectFirst {
        case Main.matchLink(h, "self") => h
      }
      val document = Jsoup.parse(new URL(from), 2000)

      val hub = hubHeader
        .orElse(Main.extractFromDocument(document).map(_._1))
        .getOrElse(throw new IllegalArgumentException("Cannot find Hub"))
      val self = selfHeader
        .orElse(Main.extractFromDocument(document).map(_._2))
        .getOrElse(throw new IllegalArgumentException("Cannot find Self"))

      await {
        ws.url(hub)
          .post(
            Map(
              "hub.mode" -> Seq("subscribe"),
              "hub.topic" -> Seq(self),
              "hub.callback" -> Seq(routes.Main.push(from).absoluteURL())
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
  val matchLink = """^<([^>]+)>; rel="(.*)"$""".r

  def extractFromDocument(document: Document): Option[(String, String)] = {

    val hubDocument =
      Option(document.select("link[rel='hub']").first()).map(_.attr("href"))
    val selfDocument =
      Option(document.select("link[rel='self']").first()).map(_.attr("href"))

    for {
      a <- hubDocument
      b <- selfDocument
    } yield a -> b
  }
}
