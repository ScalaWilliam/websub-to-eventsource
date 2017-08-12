package controllers

import javax.inject._
import concurrent.duration._

import akka.stream.scaladsl.Source
import play.api.libs.EventSource.Event
import play.api.mvc.InjectedController

class Main @Inject()() extends InjectedController {
  def index = TODO

  def events(from: String) = Action {
    Ok.chunked(Source.tick(5.seconds, 5.seconds, Event("Test")))
  }
}
