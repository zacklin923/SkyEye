package controllers;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import org.apache.hadoop.util.Shell;
import play.Logger;
import play.libs.Json;
import play.mvc.Controller;
import play.mvc.Result;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.*;

public class HDFSDUController extends Controller {

    public static Result dailydu(String path) {

        List<JsonNode> result = new ArrayList<JsonNode>();

        JsonNode logs = duOfPath(path);
        if (logs.isArray()) {

            for (int i = 14; i >= 0; i--) {
                ObjectNode eachday = Json.newObject();

                Calendar cal = Calendar.getInstance();//使用默认时区和语言环境获得一个日历。
                cal.add(Calendar.DAY_OF_MONTH, -i);//取当前日期的前一天.
                Date date = cal.getTime();
                SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
                String dateStr = sdf.format(date);
                eachday.put("date", dateStr);

                for (JsonNode log : logs) {
                    String logdate = log.get("pathSuffix").asText();
                    String fullPath = log.get("fullPath").asText();
                    String logName = fullPath.substring(0, fullPath.lastIndexOf("/")).replaceAll("/", "_").replaceAll("-", "_").replace(".", "_");
                    Long size = log.get("size").asLong() / 1024 / 1024 / 1024;
                    if (logdate.endsWith(dateStr)) {
                        eachday.put(logName, size);
                    }

                }
                result.add(eachday);
            }
        }

        return ok(Json.toJson(result));
    }

    public static Result du(final String path) {
        return ok(Json.toJson(duOfPath(path)));
    }

    public static JsonNode duOfPath(final String path) {

        List<JsonNode> result = new ArrayList<JsonNode>();

        Shell.ShellCommandExecutor shExec = null;
        // Setup command to run
        String[] command = {"sudo", "-iu", "hdfs", "hadoop", "fs", "-du", path};

        Logger.info("executing command: " + Arrays.toString(command));
        try {

            shExec = new Shell.ShellCommandExecutor(command, new File("/tmp"));
            shExec.execute();

            int exitCode = shExec.getExitCode();
            Logger.warn("Exit code from command is : " + exitCode);
            String message = shExec.getOutput();
            String[] lines = message.split("\n");
            for (String line : lines) {

                ObjectNode eachfile = Json.newObject();
                eachfile.put("parent", path);
                String fullPath = line.split("\\s+")[1];
                String size = line.split("\\s+")[0];
                eachfile.put("pathSuffix", fullPath.substring(fullPath.lastIndexOf("/") + 1));
                eachfile.put("fullPath", fullPath);
                eachfile.put("size", Long.valueOf(size));

                result.add(eachfile);
            }

        } catch (IOException e) {

            int exitCode = shExec.getExitCode();
            String message = shExec.getOutput();
            Logger.error("IOException when running command : " + Arrays.toString(command) + ", exitcode is " + exitCode + ", " + e.getMessage() + ", message: " + message);

        }

        return Json.toJson(result);
    }

}
