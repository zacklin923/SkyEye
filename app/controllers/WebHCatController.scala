package controllers

/**
 * Created by zhugb on 15-8-10.
 */

import play.api.Play.current
import play.api._
import play.api.libs.concurrent.Execution.Implicits._
import play.api.libs.json.{JsValue, Json}
import play.api.libs.ws._
import play.api.mvc._

object WebHCatController extends Controller {

  val webhcat_http_address: String = Play.application.configuration.getString("hadoop.hcatalog.webhcat.http.address").get
  val serviceRoot: String = "http://" + webhcat_http_address + "/templeton/v1"

  def databases = Action.async {
    WS.url(serviceRoot + "/ddl/database")
      .withQueryString("user.name" -> "hive")
      .get()
      .map {
      response => {
        val dbs = response.json.\("databases").as[Seq[JsValue]].map(database => Json.obj("database" -> database))
        Ok(Json.toJson(dbs))
      }
    }
  }

  def tables(database: String) = Action.async {
    WS.url(serviceRoot + "/ddl/database/" + database + "/table")
      .withQueryString("user.name" -> "hive")
      .get()
      .map {
      response => {
        val tables = response.json.\("tables").as[Seq[JsValue]].map(table => Json.obj("database" -> database, "table" -> table))
        Ok(Json.toJson(tables))
      }
    }
  }

  def partitions(database: String, table: String) = Action.async {
    WS.url(serviceRoot + "/ddl/database/" + database + "/table/" + table + "/partition")
      .withQueryString("user.name" -> "hive")
      .get()
      .map {
      response => {
        val partitions = response.json.\("partitions").as[Seq[JsValue]].map(partition => Json.obj("database" -> database, "table" -> table, "partition" -> partition.\("name")))
        Ok(Json.toJson(partitions))
      }
    }
  }

  def columns(database: String, table: String) = Action.async {
    WS.url(serviceRoot + "/ddl/database/" + database + "/table/" + table)
      .withQueryString("user.name" -> "hive")
      .get()
      .map {
      response => {
        val columns = response.json.\("columns").as[Seq[JsValue]]
        Ok(Json.toJson(columns))
      }
    }
  }

  def properties(database: String, table: String) = Action.async {
    WS.url(serviceRoot + "/ddl/database/" + database + "/table/" + table + "/property")
      .withQueryString("user.name" -> "hive")
      .get()
      .map {
      response => {
        val properties = response.json.\("properties")
        Ok(Json.toJson(properties))
      }
    }
  }


}
