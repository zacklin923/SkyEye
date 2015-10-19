package controllers;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ObjectNode;

import java.beans.PropertyVetoException;
import java.sql.*;

import com.mchange.v2.c3p0.ComboPooledDataSource;
import play.*;
import play.data.*;
import play.libs.Json;
import play.mvc.*;

public class HiveJDBCController extends AbstractJDBCController {

    static final String JDBC_DRIVER = "org.apache.hive.jdbc.HiveDriver";

    static final String DB_URL = Play.application().configuration().getString("hive.thriftserver.jdbc.url");
    static final String USER = Play.application().configuration().getString("hive.thriftserver.jdbc.user");
    static final String PASSWORD = Play.application().configuration().getString("hive.thriftserver.jdbc.password");

    static ComboPooledDataSource cpds = null;

    static {
        Logger.info("Initializeing Hive JDBC Connection Pool ...");
        cpds = new ComboPooledDataSource();

        try {
            cpds.setDriverClass(JDBC_DRIVER);
            cpds.setJdbcUrl(DB_URL);
            cpds.setUser(USER);
            cpds.setPassword(PASSWORD);
            cpds.setMaxPoolSize(5);
            cpds.setMinPoolSize(1);
            cpds.setAcquireIncrement(2);
            cpds.setCheckoutTimeout(5000);
            cpds.setIdleConnectionTestPeriod(120);
            cpds.setUnreturnedConnectionTimeout(60);
            cpds.setMaxIdleTime(3600);
            cpds.setMaxIdleTimeExcessConnections(3600);
            cpds.setMaxConnectionAge(3600);
            cpds.setMaxStatements(0);
            cpds.setMaxStatementsPerConnection(0);
        } catch (PropertyVetoException e) {
            e.printStackTrace();
            Logger.error("ERROR initializing Hive JDBC Connection Pool : " + e.getLocalizedMessage());
        }
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

        Connection conn = null;
        Statement stmt = null;
        ResultSet rs = null;
        try {

            conn = cpds.getConnection();
            stmt = conn.createStatement();

            Logger.info("RUNNING Hive SQL : " + sql);
            rs = stmt.executeQuery(sql);

            Logger.info("Done Executing Hive SQL, convert to Json.");

            JsonNode result = resultSet2Json(rs);
            response.put("retcode", 0);

            response.put("message", "OK, results are as follows");
            response.put("result", result);
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
            }
            try {
                if (conn != null)
                    conn.close();
            } catch (SQLException se) {
                se.printStackTrace();
                Logger.error("SQL Exception: " + se.getLocalizedMessage());
            }// nothing we can do
        }//end try

    }

}
