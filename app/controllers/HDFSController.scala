package controllers

import java.io.File
import java.text.SimpleDateFormat
import java.util.Date

import com.fasterxml.jackson.annotation.JsonValue
import com.fasterxml.jackson.databind.JsonNode
import org.apache.hadoop.util.Shell
import play.api._
import play.api.libs.json._
import play.api.mvc._
import play.api.Play.current
import play.api.libs.ws._
import play.api.libs.ws.ning.NingAsyncHttpClientConfigBuilder
import play.api.libs.concurrent.Execution.Implicits._
import scala.concurrent.Future

/**
 * Created by guangbin on 2015/8/8.
 */
object HDFSController extends Controller {

  val namenode_http_address = Play.application.configuration.getString("hadoop.hdfs.namenode.http.address").get
  val namenode_jmx_url = "http://" + namenode_http_address + "/jmx"
  val webhdfs_base_url = "http://" + namenode_http_address + "/webhdfs/v1"

  def nodes = Action.async {
    WS.url(namenode_jmx_url)
      .get()
      .map {
      response => {
        val fsState = response.json.\("beans")(34)
        Ok(Json.arr(
          Json.obj("category" -> "live", "value" -> fsState \ "NumLiveDataNodes"),
          Json.obj("category" -> "dead", "value" -> fsState \ "NumDeadDataNodes")
        )
        )
      }
    }
  }

  def capacity = Action.async {
    WS.url(namenode_jmx_url)
      .get()
      .map {
      response => {
        val fsState = response.json.\("beans")(9)
        Ok(Json.arr(
          Json.obj("category" -> "used", "value" -> fsState \ "CapacityUsedGB"),
          Json.obj("category" -> "remaining", "value" -> fsState \ "CapacityRemainingGB")
        )
        )
      }
    }
  }

  def blocks = Action.async {
    WS.url(namenode_jmx_url)
      .get()
      .map {
      response => {
        val fsState = response.json.\("beans")(9)
        Ok(Json.arr(
          Json.obj("category" -> "used", "value" -> fsState \ "BlocksTotal"),
          Json.obj("category" -> "remaining", "value" -> ((fsState \ "BlockCapacity").as[Int] - (fsState \ "BlocksTotal").as[Int])),
          Json.obj("category" -> "missing", "value" -> fsState \ "MissingBlocks"),
          Json.obj("category" -> "corrupt", "value" -> fsState \ "CorruptBlocks")
        )
        )
      }
    }
  }

  def listStatus(pathSuffix: String) = Action.async {
    WS.url(webhdfs_base_url + pathSuffix)
      .withQueryString("user.name" -> "hdfs", "op" -> "LISTSTATUS")
      .get()
      .map { response => Ok(response.json \ "FileStatuses" \ "FileStatus") }
  }

  def contentSummary(pathSuffix: String) = Action.async {
    WS.url(webhdfs_base_url + pathSuffix)
      .withQueryString("user.name" -> "hdfs", "op" -> "GETCONTENTSUMMARY")
      .get()
      .map { response => Ok(response.json \ "ContentSummary") }
  }

  def du(path: String) = Action {

    Ok(duOfPath(path))
  }

  def duOfPath(path: String): play.api.libs.json.JsValue = {
    val command = Array("sudo", "-iu", "hdfs", "hadoop", "fs", "-du", path)
    val shExec = new Shell.ShellCommandExecutor(command, new File("/tmp"))
    shExec.execute()
    val exitCode = shExec.getExitCode()
    val message = shExec.getOutput()
    val result = message.split("\n").map(line => Json.obj("parent" -> path, "fullPath" -> line.split("\\s+")(1), "size" -> line.split("\\s+")(0)))
    return Json.toJson(result)
  }

}
