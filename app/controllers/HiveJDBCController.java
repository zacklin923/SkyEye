package controllers;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ObjectNode;

import java.sql.*;

import play.*;
import play.data.*;
import play.libs.Json;
import play.mvc.*;

public class HiveJDBCController extends AbstractJDBCController {

    static final String JDBC_DRIVER = "org.apache.hive.jdbc.HiveDriver";

    static final String DB_URL = Play.application().configuration().getString("hive.thriftserver.jdbc.url");
    static final String USER = Play.application().configuration().getString("hive.thriftserver.jdbc.user");
    static final String PASSWORD = Play.application().configuration().getString("hive.thriftserver.jdbc.password");

    static Connection conn = null;

    private static Connection getConnection() throws ClassNotFoundException, SQLException {
        if (conn == null) {
            Logger.info("Initializeing Hive JDBC Connection ...");
            Class.forName(JDBC_DRIVER);
            conn = DriverManager.getConnection(DB_URL, USER, PASSWORD);
        }
        Logger.info("Got a Hive JDBC Connection ..");
        return conn;
    }


    public static Result runSQLQuery() {

        DynamicForm request = Form.form().bindFromRequest();
        String sql = request.get("sql").trim();
        Logger.info("get Hive SQL query request : " + sql);

        JsonNode response = null;

        try {
            // run sql
            response = executeSQL(sql);
            Logger.info("DONE executing Hive sql : " + sql);
            return ok(response);
        } catch (Exception e) {
            Logger.error("ERROR executing Hive sql : " + sql + ", Internal Server Exception" + e.getMessage());

            ObjectNode errResponse = Json.newObject();
            errResponse.put("retcode", response.get("retcode"));
            errResponse.put("message", "Internal Server Exception: " + response.get("message"));
            return ok(errResponse);
        }

    }


    private static JsonNode executeSQL(String sql) {

        ObjectNode response = Json.newObject();

        Statement stmt = null;
        ResultSet rs = null;
        try {

            conn = getConnection();
            stmt = conn.createStatement();

            Logger.info("RUNNING Hive SQL : " + sql);
            rs = stmt.executeQuery(sql);

            Logger.info("Done Executing Hive SQL, convert to Json.");

            JsonNode result = resultSet2Json(rs);
            response.put("retcode", 0);

            response.put("message", "OK, results are as follows");
            response.put("result", result);
            return response;

        } catch (ClassNotFoundException e) {
            e.printStackTrace();
            Logger.error("ClassNotFount Exception, please check your classpath whether including hive-jdbc driver class : " + e.getMessage());

            response.put("retcode", -1);
            response.put("message", e.getMessage());
            return response;
        } catch (SQLException se) {
            //Handle errors for JDBC
            se.printStackTrace();
            Logger.error("SQL Exception: " + se.getMessage());

            response.put("retcode", 1);
            response.put("message", se.getMessage());
            return response;
        } finally {
            //finally block used to close resources
            try {
                if (rs != null)
                    rs.close();
            } catch (SQLException se) {
                se.printStackTrace();
                Logger.error("SQL Exception: " + se.getLocalizedMessage());
            }
            try {
                if (stmt != null)
                    stmt.close();
            } catch (SQLException se) {
                se.printStackTrace();
                Logger.error("SQL Exception: " + se.getLocalizedMessage());
            }// nothing we can do
        }//end try

    }

}
