package controllers;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ObjectNode;

import java.io.*;
import java.sql.*;
import java.util.*;
import java.util.Date;

import com.mchange.v2.c3p0.ComboPooledDataSource;

import java.beans.PropertyVetoException;

import models.*;

import org.apache.hadoop.util.Shell;

import play.*;
import play.data.*;
import play.libs.Json;
import play.mvc.*;

public class SparkSQLJDBCController extends AbstractJDBCController {

    static final String JDBC_DRIVER = "org.apache.hive.jdbc.HiveDriver";

    static final String DB_URL = Play.application().configuration().getString("sparksql.thriftserver.jdbc.url");
    static final String USER = Play.application().configuration().getString("sparksql.thriftserver.jdbc.user");
    static final String PASSWORD = Play.application().configuration().getString("sparksql.thriftserver.jdbc.password");
    static final String WEBUI_URL = Play.application().configuration().getString("sparksql.webui.url");

    static ComboPooledDataSource cpds = null;
    static Connection conn = null;

    static {
        Logger.info("Initializeing SparkSQL JDBC Connection Pool ...");
        cpds = new ComboPooledDataSource();

        try {
            cpds.setDriverClass(JDBC_DRIVER);
            cpds.setJdbcUrl(DB_URL);
            cpds.setUser(USER);
            cpds.setPassword(PASSWORD);
            cpds.setMaxPoolSize(15);
            cpds.setMinPoolSize(10);
            cpds.setAcquireIncrement(3);
            cpds.setIdleConnectionTestPeriod(120);
            cpds.setMaxIdleTime(3600);
        } catch (PropertyVetoException e) {
            e.printStackTrace();
            Logger.error("ERROR initializing SparkSQL JDBC Connection Pool : " + e.getLocalizedMessage());
        }
    }

    public static Result getServerWebUIURL() {
        ObjectNode response = Json.newObject();

        if (WEBUI_URL != null) {
            response.put("retcode", 0);
            response.put("message", "SparkSQL Server is Ready ...");
            response.put("url", WEBUI_URL);

            return ok(response);
        } else {
            response.put("retcode", 1);
            response.put("message", "SparkSQL Server is not running or sparksql.webui.url is not configured .");
            response.put("url", "");

            return ok(response);
        }
    }


    public static Result runSQLQuery(Boolean save) {

        DynamicForm request = Form.form().bindFromRequest();
        String sql = request.get("sql").trim();
        Logger.info("get SparkSQL query request : " + sql);

        SparkSQLHistroy histroy = null;
        JsonNode response = null;

        try {
            // log history to db for analysis
            histroy = new SparkSQLHistroy();
            histroy.setUser("hadoop");
            histroy.setSqlstr(sql);
            histroy.setStartTime(new Date());
            histroy.save();
            // run sql
            response = executeSQL(histroy.getId(), sql, save);
            Logger.info("DONE executing SparkSQL sql : " + sql);
            return ok(response);
        } catch (Exception e) {
            Logger.error("ERROR executing SparkSQL sql : " + sql + ", Internal Server Exception" + e.getMessage());
            ObjectNode errResponse = Json.newObject();
            errResponse.put("retcode", -1);
            errResponse.put("message", "Internal Server Exception: " + e.getMessage());
            return ok(errResponse);
        } finally {

            // update history to db
            String savedFilename = histroy.getId() + ".result.json";
            histroy.setFinishTime(new Date());
            histroy.setMessage(response.get("message") != null ? response.get("message").asText() : "");
            histroy.setRetcode(response.get("retcode") != null ? response.get("retcode").asInt() : -1);
            histroy.setResultFile(savedFilename);
            histroy.save();

            // not saving to tmp table, save it locally
            if (!save) {
                // save result
                if (response != null) {
                    saveResult(response, "public/results/" + savedFilename);
                }
            }

        }

    }


    public static JsonNode executeSQL(Long execId, String sql, Boolean save) {

        String resulttable = "tmp.result_" + execId;

        ObjectNode response = Json.newObject();

        Statement stmt = null;
        ResultSet rs = null;
        try {

            conn = cpds.getConnection();
            stmt = conn.createStatement();

            String sqlexec = save ? "CREATE TABLE " + resulttable + " AS " + sql : sql;

            Logger.info("RUNNING Spark SQL : " + sqlexec);
            rs = stmt.executeQuery(sqlexec);

            JsonNode result = resultSet2Json(rs);
            response.put("retcode", 0);

            if (save) {
                response.put("message", "OK, results saved as table : " + resulttable);
                response.put("resulttable", resulttable);
            } else {
                response.put("message", "OK, results are as follows");
                response.put("result", result);
            }
            return response;

        } catch (SQLException se) {
            //Handle errors for JDBC
            se.printStackTrace();
            Logger.error("SQL Exception: " + se.getMessage());

            String message = se.getMessage();
            String[] m = message.split(":");
            String cause = m[0].trim();
            if ("org.apache.spark.sql.AnalysisException".equals(cause)) {
                response.put("retcode", 1);
            } else if ("org.apache.spark.SparkException".equals(cause)) {
                response.put("retcode", 2);
            } else if ("org.apache.hadoop.hive.ql.metadata.HiveException".equals(cause) || "org.apache.thrift.transport.TTransportException".equals(cause) || "org.apache.thrift.TApplicationException".equals(cause)) {
                response.put("retcode", 3);
            } else {
                // unknown exception
                response.put("retcode", 100);
            }
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


    public static Result fetchResult(String resulttable, String limit) {

        ObjectNode response = Json.newObject();

        String execId = resulttable.split("_")[1];

        Statement stmt = null;
        ResultSet rs = null;
        try {

            conn = cpds.getConnection();
            stmt = conn.createStatement();

            String sqlexec = "SELECT * FROM " + resulttable + " LIMIT " + limit;
            Logger.info("RUNNING SQL : " + sqlexec);
            rs = stmt.executeQuery(sqlexec);

            JsonNode result = resultSet2Json(rs);
            response.put("retcode", 0);
            response.put("message", "OK, top " + limit + " results fetched back. <p> You should download all result <a href='/rest/sparksql/allresults?execId=" + execId + "' target='_blank'><b style='color: red;'>here</b></a>");
            response.put("result", result);
            return ok(response);

        } catch (SQLException se) {
            //Handle errors for JDBC
            se.printStackTrace();
            Logger.error("SQL Exception: " + se.getMessage());

            String message = se.getMessage();
            String[] m = message.split(":");
            String cause = m[0].trim();
            if ("org.apache.spark.sql.AnalysisException".equals(cause)) {
                response.put("retcode", 1);
            } else if ("org.apache.spark.SparkException".equals(cause)) {
                response.put("retcode", 2);
            } else if ("org.apache.hadoop.hive.ql.metadata.HiveException".equals(cause) || "org.apache.thrift.transport.TTransportException".equals(cause) || "org.apache.thrift.TApplicationException".equals(cause)) {
                response.put("retcode", 3);
            } else {
                // unknown exception
                response.put("retcode", 100);
            }
            response.put("message", se.getMessage());
            return ok(response);

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

            // save result
            if (response != null) {
                saveResult(response, "public/results/" + execId + ".result.json");
            }
        }//end try

    }

    public static void saveResult(JsonNode result, String filename) {
        Writer writer = null;

        try {
            writer = new BufferedWriter(new OutputStreamWriter(
                    new FileOutputStream(filename), "utf-8"));
            writer.write(result.toString());
        } catch (IOException ex) {
            // report
            Logger.warn("error saving result to file : " + filename + " , " + ex.getMessage());
        } finally {
            try {
                writer.close();
            } catch (Exception ex) {
            }
        }
    }

    public static Result downloadAllResults(String execId) {
        response().setContentType("application/x-download");
        response().setHeader("Content-disposition", "attachment; filename=result_" + execId + ".tsv");

        File resultfile = new File(Play.application().path(), "public/results/" + execId + ".result.all");
        if (!resultfile.exists()) {
            fetchHiveData("tmp.result_" + execId, resultfile.getAbsolutePath());
        }
        if (resultfile.exists()) {
            return ok(resultfile);
        } else {
            return ok("ERROR, File Not Found");
        }
    }

    private static void fetchHiveData(final String table, final String resultfile) {

        List<JsonNode> result = new ArrayList<JsonNode>();

        String sql = "set hive.cli.print.header=true;select * from " + table;

        Shell.ShellCommandExecutor shExec = null;
        // Setup command to run
        String[] command = {"/opt/hive/bin/hive", "-e", "\"" + sql + "\""};

        Logger.info("executing command: " + Arrays.toString(command));
        try {

            shExec = new Shell.ShellCommandExecutor(command, new File("/tmp"));
            shExec.execute();

            int exitCode = shExec.getExitCode();
            Logger.warn("Exit code from command is : " + exitCode);
            String message = shExec.getOutput();

            Writer writer = null;
            try {
                writer = new BufferedWriter(new OutputStreamWriter(
                        new FileOutputStream(resultfile), "utf-8"));
                writer.write(message);
            } catch (IOException ex) {
                // report
                ex.printStackTrace();
                Logger.warn("error saving result to file : " + resultfile + " , " + ex.getMessage());
            } finally {
                try {
                    writer.close();
                } catch (Exception ex) {
                }
            }

        } catch (IOException e) {
            int exitCode = shExec.getExitCode();
            String message = shExec.getOutput();
            Logger.error("IOException when running command : " + Arrays.toString(command) + ", exitcode is " + exitCode + ", " + e.getMessage() + ", message: " + message);
        }
    }

}
