package controllers

/**
 * Created by zhugb on 15-8-10.
 */

import play.api._
import play.api.libs.json.{JsValue, Json}
import play.api.mvc._
import play.api.Play.current
import play.api.libs.ws._
import play.api.libs.ws.ning.NingAsyncHttpClientConfigBuilder
import play.api.libs.concurrent.Execution.Implicits._
import scala.concurrent.Future

object YarnController extends Controller {

  val resourcemanager_http_addresss = Play.application.configuration.getString("hadoop.yarn.resourcemanager.http.address").get
  val serviceRoot = "http://" + resourcemanager_http_addresss + "/ws/v1/cluster/"

  def nodes = Action.async {
    WS.url(serviceRoot + "metrics")
      .get()
      .map {
      response => {
        val clusterMetrics = response.json.\("clusterMetrics")
        Ok(Json.arr(
          Json.obj("category" -> "active", "value" -> clusterMetrics \ "activeNodes"),
          Json.obj("category" -> "lost", "value" -> clusterMetrics \ "lostNodes")
        )
        )
      }
    }
  }

  def vcores = Action.async {
    WS.url(serviceRoot + "scheduler")
      .get()
      .map {
      response => {
        val resources = response.json.\("scheduler").\("schedulerInfo").\("rootQueue")
        val totalVcores = resources \ "clusterResources" \ "vCores"
        val usedVcores = resources \ "usedResources" \ "vCores"
        val freeVcores = totalVcores.as[Int] - usedVcores.as[Int]
        Ok(Json.arr(
          Json.obj("category" -> "used", "value" -> usedVcores),
          Json.obj("category" -> "free", "value" -> freeVcores)
        )
        )
      }
    }
  }

  def memory = Action.async {
    WS.url(serviceRoot + "scheduler")
      .get()
      .map {
      response => {
        val resources = response.json.\("scheduler").\("schedulerInfo").\("rootQueue")
        val totalMemory = resources \ "clusterResources" \ "memory"
        val usedMemory = resources \ "usedResources" \ "memory"
        val freeMemory = totalMemory.as[Int] - usedMemory.as[Int]
        Ok(Json.arr(
          Json.obj("category" -> "used", "value" -> usedMemory),
          Json.obj("category" -> "free", "value" -> freeMemory)
        )
        )
      }
    }
  }

  def appSummary = Action.async {
    WS.url(serviceRoot + "metrics")
      .get()
      .map {
      response => {
        val clusterMetrics = response.json.\("clusterMetrics")
        Ok(Json.arr(
          Json.obj("category" -> "completed", "value" -> clusterMetrics \ "appsCompleted"),
          Json.obj("category" -> "running", "value" -> clusterMetrics \ "appsRunning"),
          Json.obj("category" -> "pending", "value" -> clusterMetrics \ "appsPending"),
          Json.obj("category" -> "failed", "value" -> clusterMetrics \ "appsFailed"),
          Json.obj("category" -> "killed", "value" -> clusterMetrics \ "appsKilled")
        )
        )
      }
    }
  }

  def apps(user: String, queue: String, startedTimeBegin: String, startedTimeEnd: String,
           limit: String, state: String, finalStatus: String) = Action.async {
    WS.url(serviceRoot + "apps")
      .withQueryString("user" -> user, "queue" -> queue, "startedTimeBegin" -> startedTimeBegin,
        "startedTimeEnd" -> startedTimeEnd, "limit" -> limit, "state" -> state, "finalStatus" -> finalStatus)
      .get()
      .map {
      response => {
        Ok(response.json \ "apps" \ "app")
      }
    }
  }

}
