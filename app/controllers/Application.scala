package controllers

import play.api._
import play.api.mvc._
import play.api.libs.iteratee._
import play.api.libs.concurrent.Execution.Implicits.defaultContext

object Application extends Controller {

  def index = Action {
    Ok(views.html.index())
  }

  def socket = WebSocket.using[String] { request =>

    // Log events to the console
    val in = Iteratee.foreach[String](println).map { _ =>
      println("Disconnected")
    }

    // Send a single 'Hello!' message
    val out = Enumerator("Hello!")

    (in, out)
  }

}