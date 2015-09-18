package com.ilufang.kctServer;

import com.ilufang.kcInputDaemon.InputDaemon;
import com.ilufang.kcLogging.KCLogger;
import com.ilufang.kcUtils.Session;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import com.sun.net.httpserver.HttpServer;
import org.json.JSONArray;
import org.json.JSONObject;

import javax.swing.*;
import java.io.*;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.Buffer;
import java.nio.charset.Charset;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Created by ilufang on 5/12/15.
 */
public class KCDelegateServer {
    private static int BUFFER_SIZE = 65536;
    private static Session session;
    private static InputDaemon kcid;

    public KCDelegateServer(Session session, InputDaemon kcid) {
        this.session = session;
        this.kcid = kcid;
    }

    public void setKcid(InputDaemon kcid) {
        this.kcid = kcid;
    }

    public static void createContexts(HttpServer server) {
        server.createContext("/kcs/", new StaticHandler());
        server.createContext("/kcsapi/", new APIHandler());
        System.out.println("Started!!");
    }

    static public class StaticHandler implements HttpHandler {

        @Override
        public void handle(HttpExchange exchange) throws IOException {
            try {

                File src = new File("kcscache" + exchange.getRequestURI().toString().replace("?", "/"));

                if (!src.exists()) {
                    try {
                        src.getParentFile().mkdirs();
                        src.createNewFile();

                        URL session_url = new URL(session.url);
                        URL request_url = new URL(session_url.getProtocol(), session_url.getHost(), exchange.getRequestURI().toString());
                        HttpURLConnection con = (HttpURLConnection) request_url.openConnection();
                        con.setRequestMethod(exchange.getRequestMethod());

                        Set<Map.Entry<String, List<String>>> req_headers_set = exchange.getRequestHeaders().entrySet();
                        for (Map.Entry<String, List<String>> entry : req_headers_set) {
                            con.setRequestProperty(entry.getKey(), entry.getValue().get(0));
                        }

                        InputStream is = con.getInputStream();
                        OutputStream os = new FileOutputStream(src);
                        byte[] buffer = new byte[BUFFER_SIZE];
                        int count;

                        while ((count = is.read(buffer)) != -1) {
                            os.write(buffer, 0, count);
                        }
                        is.close();
                        os.close();

                        File props_json = new File("kcscache" + exchange.getRequestURI().toString().replace("?","/") + ".json");
                        props_json.getParentFile().mkdirs();
                        props_json.createNewFile();
                        JSONObject props = new JSONObject();
                        for (Map.Entry<String, List<String>> entry : con.getHeaderFields().entrySet()) {
                            if (entry.getKey() != null && entry.getKey().length() > 0) {
                                props.put(entry.getKey(), entry.getValue().get(0));
                            }
                        }
                        os = new FileOutputStream(props_json);
                        os.write(props.toString().getBytes(Charset.forName("utf-8")));
                        os.close();
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
                // Read from file

                File headers_json_file = new File("kcscache" + exchange.getRequestURI().toString().replace("?","/") + ".json");
                BufferedReader reader = new BufferedReader(new InputStreamReader(new FileInputStream(headers_json_file)));
                String headers_json_text = "", line;
                while ((line = reader.readLine())!=null) {
                    headers_json_text+=line+"\n";
                }
                JSONObject headers_json = new JSONObject(headers_json_text);

                for (String key : headers_json.keySet()) {
                    exchange.getResponseHeaders().set(key, headers_json.get(key).toString());
                }

                exchange.sendResponseHeaders(200, src.length());
                OutputStream os = exchange.getResponseBody();
                InputStream is = new FileInputStream(src);

                byte[] buffer = new byte[BUFFER_SIZE];
                int count;
                while ((count = is.read(buffer)) != -1) {
                    os.write(buffer, 0, count);
                }

                is.close();
                os.close();
            } catch (IOException e) {
                //e.printStackTrace();
                throw e;
            }
        }
    }
    static public class APIHandler implements HttpHandler {
        @Override
        public void handle(HttpExchange exchange) throws IOException {

            if (exchange.getRequestURI().toString().startsWith("/kcsapi/api_start2")) {
                // just give shipsdb.json, no loading.
                File src = new File("shipsdb.json");
                exchange.getResponseHeaders().set("Content-Type","text/json;charset=utf-8");
                exchange.sendResponseHeaders(200, src.length());
                OutputStream os = exchange.getResponseBody();
                InputStream is = new FileInputStream(src);
                byte[] buffer = new byte[BUFFER_SIZE];
                int count;
                while ((count = is.read(buffer))!=-1) {
                    os.write(buffer, 0, count);
                }
                is.close();
                os.close();
                return;
            }

            try {
                URL session_url = new URL(session.url);
                URL request_url = new URL(session_url.getProtocol(), session_url.getHost(), exchange.getRequestURI().toString());
                HttpURLConnection con = (HttpURLConnection) request_url.openConnection();

                con.setDoOutput(true);
                con.setDoInput(true);
                con.setRequestMethod(exchange.getRequestMethod());


                Set<Map.Entry<String, List<String>>> req_headers_set = exchange.getRequestHeaders().entrySet();
                for (Map.Entry<String, List<String>> entry : req_headers_set) {
                    String value = entry.getValue().get(0);
                    value = value.replaceAll("localhost",session_url.getHost());
                    value = value.replaceAll(exchange.getLocalAddress().getAddress().getHostAddress(), session_url.getHost());
                    con.setRequestProperty(entry.getKey(), value);
                }


                InputStream post_in = exchange.getRequestBody();
                OutputStream post_out = con.getOutputStream();
                BufferedReader reader = new BufferedReader(new InputStreamReader(post_in));
                String postdata = "", line;
                byte[] buffer = new byte[BUFFER_SIZE];
                int count;
                while ((line = reader.readLine())!=null) {
                    postdata+=line+'\n';
                }
                post_in.close();
                post_out.write(postdata.getBytes(Charset.forName("utf-8")));
                post_out.close();

                InputStream is = con.getInputStream();
                reader = new BufferedReader(new InputStreamReader(is));
                String data = "";
                while ((line = reader.readLine()) != null) {
                    data += line+"\n";
                }
                is.close();


                if (exchange.getRequestURI().toString().startsWith("/kcsapi/api_port/port")) {
                    // Port handler: update fleet info, etc.
                    JSONObject portdata = new JSONObject(data.substring(7));
                    JSONArray ships = portdata.getJSONObject("api_data").getJSONArray("api_ship");
                    session.fleet.update(ships);
                    for (int i=0; i<ships.length(); i++) {
                        if (ships.getJSONObject(i).getInt("api_fuel")==0) {
                            // set fuel to 1 bar
                            int shipid = ships.getJSONObject(i).getInt("api_ship_id");
                            int maxval = session.db.getShipData(shipid).getInt("api_fuel_max");
                            ships.getJSONObject(i).put("api_fuel",maxval/10);
                        }
                    }
                    for (int i=0; i<ships.length(); i++) {
                        if (ships.getJSONObject(i).getInt("api_bull")==0) {
                            // set fuel to 1 bar
                            int shipid = ships.getJSONObject(i).getInt("api_ship_id");
                            int maxval = session.db.getShipData(shipid).getInt("api_bull_max");
                            ships.getJSONObject(i).put("api_bull",maxval/10);
                        }
                    }
                    data = "svdata="+portdata.toString();
                }

                exchange.sendResponseHeaders(200, data.getBytes(Charset.forName("utf-8")).length);
                OutputStream os = exchange.getResponseBody();
                os.write(data.getBytes(Charset.forName("utf-8")));
                os.close();


                File props_json = new File("kcscache" + exchange.getRequestURI() + ".json");
                props_json.getParentFile().mkdirs();
                props_json.createNewFile();
                JSONObject props = new JSONObject();
                for (Map.Entry<String, List<String>> entry : con.getHeaderFields().entrySet()) {
                    if (entry.getKey() != null && entry.getKey().length() > 0) {
                        props.put(entry.getKey(), entry.getValue().get(0));
                    }
                }
                // Send request to loggers
                for (KCLogger logger : session.loggers) {
                    logger.processRequest(exchange.getRequestURI().toString(), postdata, data);
                }
            } catch (Exception e) {
                //e.printStackTrace();
            }

            // Handle Specific events
            if (exchange.getRequestURI().toString().startsWith("/kcsapi/api_port/port")) {
                kcid.port_received = true;
            } else if (exchange.getRequestURI().toString().startsWith("/kcsapi/api_get_member/ndock")) {
                kcid.dock_received = true;
            }

        }
    }
}
