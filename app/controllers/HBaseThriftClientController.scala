package controllers

import java.nio.ByteBuffer
import java.util

import org.apache.hadoop.hbase.thrift2.generated._
import org.apache.hadoop.hbase.util.Bytes
import org.apache.thrift.protocol.TBinaryProtocol
import org.apache.thrift.transport.TSocket
import play.api.Play.current
import play.api._
import play.api.libs.concurrent.Execution.Implicits._
import play.api.libs.json.{JsObject, Json}
import play.api.libs.ws._
import play.api.mvc._

import scala.collection.JavaConversions._

object HBaseThriftClientController extends Controller {

  val hbases_thrift2_server_host = Play.application.configuration.getString("hbase.thrift2.server.host").get
  val hbases_thrift2_server_port = Play.application.configuration.getString("hbase.thrift2.server.port").get.toInt

  val transport = new TSocket(hbases_thrift2_server_host, hbases_thrift2_server_port, 10000)
  val protocol = new TBinaryProtocol(transport)
  val client = new THBaseService.Client(protocol)

  def scan(table: String, start: Long, end: Long, mvid: String) = Action {
    val ret = new util.ArrayList[JsObject]()

    try {
      transport.open()
      val scan = new TScan()
      scan.setBatchSize(1000)
      scan.setCaching(1000)
      val timerange = new TTimeRange()
      timerange.setMinStamp(start)
      timerange.setMaxStamp(end)
      scan.setTimeRange(timerange)
      if (mvid != null) {
        scan.setStartRow(Bytes.toBytes(mvid))
        scan.setStopRow(Bytes.toBytes(mvid))
      }

      val result = client.getScannerResults(ByteBuffer.wrap(Bytes.toBytes(table)), scan, 1000)

      for (r: TResult <- result) {

        val mvid = new String(r.getRow)
        for (v: TColumnValue <- r.getColumnValues) {
          val f = new String(v.getFamily)
          val qualifier = ByteBuffer.wrap(v.getQualifier).asCharBuffer().toString
          val value = ByteBuffer.wrap(v.getValue).getLong
          val ts = v.getTimestamp
          val item = Json.obj("id" -> value, "group" -> mvid, "content" -> f, "start" -> ts, "type" -> "point")
          ret.add(item)
        }
      }
    } finally {
      transport.close()
    }

    Ok(Json.toJson(ret.toSet))
  }

  def get(table: String, showRequestId: Long, family: String, qualifier: String) = Action {
    val ret = new util.ArrayList[JsObject]()

    val get = new TGet()
    get.setRow(Bytes.toBytes(showRequestId.toString.hashCode))
    val col = new TColumn()
    col.setFamily(Bytes.toBytes(family))
    col.setQualifier(Bytes.toBytes(qualifier))
    get.setColumns(Seq(col))

    val result = client.get(ByteBuffer.wrap(Bytes.toBytes(table)), get)

    val rowkey = ByteBuffer.wrap(result.getRow).toString
    for (v: TColumnValue <- result.getColumnValues) {
      val f = ByteBuffer.wrap(v.getFamily).toString
      val qualifier = ByteBuffer.wrap(v.getQualifier).toString
      val value = ByteBuffer.wrap(v.getValue).toString
      val ts = v.getTimestamp
      val item = Json.obj("raw" -> value)
      ret.add(item)
    }

    Ok(Json.toJson(ret.toSet))
  }


}
