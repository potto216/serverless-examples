package example;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.BufferedReader;

import com.amazonaws.services.lambda.runtime.RequestStreamHandler;
import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.LambdaLogger;

import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;

import example.BookBox;

public class LambdaFunctionHandler implements RequestStreamHandler {
    JSONParser parser = new JSONParser();

    public void handleRequest(InputStream inputStream, OutputStream outputStream, Context context) throws IOException {
        LambdaLogger logger = context.getLogger();
        logger.log("Loading Java Lambda handler of ProxyWithStream");
        logger.log("Version 1.1");

        String proxy = null;
        boolean debug = false;
        int totalBooks = 1;
        double pricePerBox = 1;

        BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream));
        JSONObject responseJson = new JSONObject();
        String responseCode = "200";
        JSONObject event = null;

        try {

            event = (JSONObject) parser.parse(reader);

            if (event.get("pathParameters") != null) {
                JSONObject pps = (JSONObject) event.get("pathParameters");
                if (pps.get("proxy") != null) {
                    proxy = (String) pps.get("proxy");
                }
            }

            JSONObject parmsObj = (JSONObject) (event.get("params"));
            logger.log("event parms = " + (parmsObj).toJSONString());

            if (parmsObj.get("querystring") != null) {
                JSONObject qps = (JSONObject) parmsObj.get("querystring");
                logger.log("querystring is not null and is equal to " + qps.toString());
                String parm = (String) qps.get("totalBooks");
                if (parm != null) {
                    totalBooks = Integer.parseInt(parm);
                    logger.log("The totalBooks is " + parm + " which maps to " + totalBooks);
                } else {
                    logger.log("'totalBooks' was not specified.");
                }

                parm = (String) qps.get("pricePerBox");
                if (parm != null) {
                    pricePerBox = Double.parseDouble(parm);
                    logger.log("The pricePerBox is " + parm + " which maps to " + pricePerBox);
                } else {
                    logger.log("'pricePerBox' was not specified.");
                }

                parm = (String) qps.get("debug");
                if (parm != null) {
                    debug = Boolean.parseBoolean(parm);
                    logger.log("Debug flag is " + parm + " which maps to " + debug);
                } else {
                    logger.log("'debug' was not specified.");
                }

            } else {
                logger.log("querystring is null. parmsObj = " + parmsObj.toJSONString());
            }

        } catch (Exception pex) {
            responseJson.put("statusCode", "400");
            responseJson.put("exception", pex);
            responseJson.put("exception-stack-trace", LambdaFunctionHandler.getStackTrace(pex));

        }

        int output = 0;
        JSONObject responseBody = new JSONObject();

        responseBody.put("message",
                "The average price per book is $" + new BookBox(totalBooks, pricePerBox).calcAvgPricePerBook()
                        + " when the box contains " + totalBooks + " books and the box's price is $" + pricePerBox);
        if (debug) {
            responseBody.put("debug", debug);
            responseBody.put("event", event);
            responseBody.put("proxy", proxy);
        }

        JSONObject headerJson = new JSONObject();
        headerJson.put("x-custom-header", "my custom header value");
        headerJson.put("Access-Control-Allow-Origin", "*");

        responseJson.put("isBase64Encoded", false);
        responseJson.put("statusCode", responseCode);
        responseJson.put("headers", headerJson);
        responseJson.put("body", responseBody);

        OutputStreamWriter writer = new OutputStreamWriter(outputStream, "UTF-8");
        writer.write(responseJson.toJSONString());
        writer.close();
    }

    private static String getStackTrace(Exception ex) {
        StringBuffer sb = new StringBuffer(500);
        StackTraceElement[] st = ex.getStackTrace();
        sb.append(ex.getClass().getName() + ": " + ex.getMessage() + "\n");
        for (int i = 0; i < st.length; i++) {
            sb.append("\t at " + st[i].toString() + "\n");
        }
        return sb.toString();
    }
}