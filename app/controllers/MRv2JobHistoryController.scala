package controllers

/**
 * Created by zhugb on 15-8-10.
 */

import play.api.Play.current
import play.api._
import play.api.libs.concurrent.Execution.Implicits._
import play.api.libs.json.Json
import play.api.libs.ws._
import play.api.mvc._

object MRv2JobHistoryController extends Controller {

  val mapreduce_http_addresss = Play.application.configuration.getString("hadoop.mapreduce.historyserver.http.address").get
  val serviceRoot = "http://" + mapreduce_http_addresss + "/ws/v1/history/mapreduce/"


  def jobhistory(user: String, queue: String, startedTimeBegin: String, startedTimeEnd: String, limit: String, state: String) = Action.async {
    WS.url(serviceRoot + "jobs")
      .withQueryString("user" -> user, "queue" -> queue, "startedTimeBegin" -> startedTimeBegin,
        "startedTimeEnd" -> startedTimeEnd, "limit" -> limit, "state" -> state)
      .get()
      .map {
      response => {
        Ok(response.json \ "jobs" \ "job")
      }
    }
  }


}
