package controllers

import java.nio.ByteBuffer
import java.util
import utils.ThriftUtil

import com.mediav.data.log.LogUtils
import com.mediav.data.log.unitedlog.UnitedEvent
import org.apache.hadoop.hbase.thrift2.generated._
import org.apache.hadoop.hbase.util.Bytes
import org.apache.thrift.TDeserializer
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

  def scan(table: String, start: Long, end: Long, row: String) = Action {

    val items = new util.ArrayList[JsObject]()
    val groups = new util.ArrayList[JsObject]()

    try {
      transport.open()
      val scan = new TScan()
      scan.setBatchSize(1000)
      scan.setCaching(1000)
      val timerange = new TTimeRange()
      timerange.setMinStamp(start)
      timerange.setMaxStamp(end)
      scan.setTimeRange(timerange)
      if (row != null) {
        scan.setStartRow(Bytes.toBytes(row))
        scan.setStopRow(Bytes.toBytes(row))
      }

      val result = client.getScannerResults(ByteBuffer.wrap(Bytes.toBytes(table)), scan, 100)

      for (r: TResult <- result) {

        val mvid = new String(r.getRow)
        val groupId = mvid.hashCode
        val group = Json.obj("id" -> groupId, "content" -> mvid)
        groups.add(group)

        for (v: TColumnValue <- r.getColumnValues) {
          val f = new String(v.getFamily)
          val qualifier = ByteBuffer.wrap(v.getQualifier).asCharBuffer().toString
          val value = ByteBuffer.wrap(v.getValue).getLong
          val ts = v.getTimestamp
          val content = (f + ":" + value.toString)

          val item = Json.obj("id" -> content.hashCode, "group" -> groupId, "content" -> content, "start" -> ts, "type" -> "box")
          items.add(item)
        }
      }
    } finally {
      transport.close()
    }

    val ret = Json.obj("items" -> Json.toJson(items.toSet), "groups" -> Json.toJson(groups.toSet))
    Ok(ret)
  }

  def get(table: String, showRequestId: Long, family: String, qualifier: String) = Action {
    val ret = new util.ArrayList[JsObject]()

    try {
      transport.open()
      val get = new TGet()
      get.setRow(Bytes.toBytes(showRequestId.toString.hashCode))
      val col = new TColumn()
      if (family != null) {
        col.setFamily(Bytes.toBytes(family))
      }
      if (qualifier != null) {
        col.setQualifier(Bytes.toBytes(qualifier))
      }
      get.setColumns(Seq(col))

      val result = client.get(ByteBuffer.wrap(Bytes.toBytes(table)), get)

      val deserializer: TDeserializer = new TDeserializer()

      val rowkey = ByteBuffer.wrap(result.getRow).toString
      for (v: TColumnValue <- result.getColumnValues) {
        val f = new String(v.getFamily)
        val qualifier = ByteBuffer.wrap(v.getQualifier).getLong()
        val value = ByteBuffer.wrap(v.getValue).toString
        val ts = v.getTimestamp

        val ue = LogUtils.thriftBinarydecoder(v.getValue,classOf[UnitedEvent])

        val item = Json.obj("logId" -> qualifier, "phase" -> f, "ts" ->ts, "raw" -> ue.toString)
        ret.add(item)
      }
    }finally {
      transport.close()
    }

    Ok(Json.toJson(ret.toSet))
  }


}
