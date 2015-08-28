package controllers;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import play.libs.Json;
import play.mvc.Controller;

import java.math.BigDecimal;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * Created by zhugb on 15-8-27.
 */
public class AbstractJDBCController extends Controller {

    protected static JsonNode resultSet2Json(ResultSet rs) throws SQLException {

        List<JsonNode> result = new ArrayList<JsonNode>();

        if (rs != null) {
            while (rs.next()) {
                result.add(mapRow(rs, 1));
            }
        }

        return Json.toJson(result);
    }

    protected static JsonNode mapRow(ResultSet rs, int rowNum) throws SQLException {
        ObjectNode objectNode = Json.newObject();
        ResultSetMetaData rsmd = rs.getMetaData();
        int columnCount = rsmd.getColumnCount();
        for (int index = 1; index <= columnCount; index++) {
            String column = rsmd.getColumnName(index);
            Object value = rs.getObject(column);
            String shortColumn = column.split("\\.").length == 2 ? column.split("\\.")[1] : column;
            if (value == null) {
                objectNode.putNull(shortColumn);
            } else if (value instanceof Integer) {
                objectNode.put(shortColumn, (Integer) value);
            } else if (value instanceof String) {
                objectNode.put(shortColumn, (String) value);
            } else if (value instanceof Boolean) {
                objectNode.put(shortColumn, (Boolean) value);
            } else if (value instanceof Date) {
                objectNode.put(shortColumn, ((Date) value).getTime());
            } else if (value instanceof Long) {
                objectNode.put(shortColumn, (Long) value);
            } else if (value instanceof Double) {
                objectNode.put(shortColumn, (Double) value);
            } else if (value instanceof Float) {
                objectNode.put(shortColumn, (Float) value);
            } else if (value instanceof BigDecimal) {
                objectNode.put(shortColumn, (BigDecimal) value);
            } else if (value instanceof Byte) {
                objectNode.put(shortColumn, (Byte) value);
            } else if (value instanceof byte[]) {
                objectNode.put(shortColumn, (byte[]) value);
            } else {
                throw new IllegalArgumentException("Unmappable object type: " + value.getClass());
            }
        }
        return objectNode;
    }
}
