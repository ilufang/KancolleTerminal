package com.ilufang.kctServer;

import com.ilufang.kcInputDaemon.InputDaemon;
import com.ilufang.kcInteractive.Battle15;
import com.ilufang.kcInteractive.E2Unryuu;
import com.ilufang.kcInteractive.ResourceFarming;
import com.ilufang.kcInteractive.Sparkle;
import com.ilufang.kcLogging.BuildLogging;
import com.ilufang.kcLogging.DropLogging;
import com.ilufang.kcLogging.KCLogger;
import com.ilufang.kcRequest.CustomRequest;
import com.ilufang.kcRequest.OrganizationRequest;
import com.ilufang.kcUtils.Deck;
import com.ilufang.kcUtils.Session;
import com.ilufang.kcUtils.Ship;
import com.sun.net.httpserver.Headers;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import com.sun.net.httpserver.HttpServer;
import org.json.JSONArray;
import org.json.JSONObject;

import java.awt.*;
import java.io.*;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.URI;
import java.net.URL;
import java.nio.charset.Charset;
import java.util.*;
import java.util.List;

/**
 * Created by ilufang on 5/3/15.
 */
public class Server {
    final private static boolean load_jar = false; // TODO Switch on debug/release

    private static boolean init_done = false;
    private static boolean use_kcid;

    private static Session session;
    private static InputDaemon kcid;

    private static int portno;

    private static HttpServer server;

    private static KCDelegateServer delegate;

    public static void beginServer(int port) throws IOException {
        server = HttpServer.create(new InetSocketAddress(port), 0);
        portno = port;
        server.createContext("/", new DefaultHandler());
        createCommandContext(server);
        server.setExecutor(null); // creates a default executor
        server.start();
        System.out.println("Server initialization sequence completed.");
    }

    public static void beginServer() throws IOException {
        int default_port = 80;
        try {
            beginServer(default_port);
        } catch (Exception e1) {
            try {
                System.out.println("FALLBACK! TRYING TO LAUNCH AS NON-ROOT.");
                System.out.println("LOCAL KCS DELEGATE SERVER WON'T WORK AS INTENDED!");
                System.out.println("PLEASE RUN JAVA WITH ROOT/ADMIN PRIVILEGES!");
                default_port = 8066;
                beginServer(default_port);
            } catch (Exception e2) {
                System.out.println("FAIL TO CREATE LOCAL SERVER!");
                System.out.println("Or server is already running?");
            }
        }
        try {
            Desktop.getDesktop().browse(new URI("http://localhost:"+default_port));
        } catch (Exception e) {
            e.printStackTrace();
            System.out.println();
            System.out.println("Please open your browser and direct to http://localhost:"+default_port);
        }
    }

    static class DefaultHandler implements HttpHandler {
        public void handle(HttpExchange t) throws IOException {
            if (t.getRequestURI().toString().equals("/")) {
                String src;
                t.getResponseHeaders().set("Content-Type","text/html;charset=utf-8");
                if (init_done) {
                    src = "www/index.html";
                } else {
                    src = "www/init.html";
                }
                InputStream is;
                if (load_jar) {
                    is = getClass().getClassLoader().getResourceAsStream(src);
                } else {
                    is = new FileInputStream(new File(src));
                }

                InputStreamReader isr = new InputStreamReader(is, Charset.forName("utf-8"));
                BufferedReader reader = new BufferedReader(isr);
                String data = "", tmp;
                while ((tmp=reader.readLine())!=null) {
                    data+=tmp+"\n";
                }
                t.sendResponseHeaders(200, data.getBytes(Charset.forName("utf-8")).length);
                OutputStream os = t.getResponseBody();
                os.write(data.getBytes(Charset.forName("utf-8")));
                is.close();
                os.close();
            } else if (t.getRequestURI().toString().startsWith("/api/")) {
                // To Be processed using specific handlers
            } else if (t.getRequestURI().toString().startsWith("/static/")) {
                File src = new File(t.getRequestURI().toString().substring("/static/".length()));
                System.out.println(src);
                // For loose file
                FileInputStream is = new FileInputStream(src);
                Headers headers = t.getResponseHeaders();
                if (src.getPath().toString().endsWith(".html")) {
                    headers.set("Content-Type", "text/html;charset=utf-8");
                } else if (src.getPath().toString().endsWith(".css")) {
                    headers.set("Content-Type", "text/css;charset=utf-8");
                } else if (src.getPath().toString().endsWith(".js")) {
                    headers.set("Content-Type", "application/javascript;charset=utf-8");
                } else if (src.getPath().toString().endsWith(".png")) {
                    headers.set("Content-Type", "image/png");
                } else if (src.getPath().toString().endsWith(".jpg")) {
                    headers.set("Content-Type", "image/jpeg");
                } else if (src.getPath().toString().endsWith(".bmp")) {
                    headers.set("Content-Type", "image/bmp");
                }
                t.sendResponseHeaders(200, src.length());
                OutputStream os = t.getResponseBody();
                byte[] buffer = new byte[65536];
                int count;
                while ((count=is.read(buffer))!=-1) {
                    os.write(buffer,0,count);
                }
                is.close();
                os.close();
            } else {
                String src = "www"+t.getRequestURI();

                InputStream is;
                if (load_jar) {
                    is = getClass().getClassLoader().getResourceAsStream(src);
                } else {
                    is = new FileInputStream(new File(src));
                }

                Headers headers = t.getResponseHeaders();
                if (src.endsWith(".html")) {
                    headers.set("Content-Type", "text/html;charset=utf-8");
                } else if (src.endsWith(".css")) {
                    headers.set("Content-Type", "text/css;charset=utf-8");
                } else if (src.endsWith(".js")) {
                    headers.set("Content-Type", "application/javascript;charset=utf-8");
                } else if (src.endsWith(".png")) {
                    headers.set("Content-Type", "image/png");
                } else if (src.endsWith(".jpg")) {
                    headers.set("Content-Type", "image/jpeg");
                } else if (src.endsWith(".bmp")) {
                    headers.set("Content-Type", "image/bmp");
                }
                InputStreamReader isr = new InputStreamReader(is, Charset.forName("utf-8"));
                BufferedReader reader = new BufferedReader(isr);
                String data = "", tmp;
                while ((tmp=reader.readLine())!=null) {
                    data+=tmp+"\n";
                }
                t.sendResponseHeaders(200, data.getBytes(Charset.forName("utf-8")).length);
                OutputStream os = t.getResponseBody();
                os.write(data.getBytes(Charset.forName("utf-8")));
                is.close();
                os.close();
            }
        }
    }


    static void createCommandContext(HttpServer server) {
        server.createContext("/api/shutdown", new KCShutdownHandler());
        server.createContext("/api/init",new KCInitHandler()).getFilters().add(new ParameterFilter());
        server.createContext("/api/dbload", new KCDBLoadHandler());
        server.createContext("/api/userinfo", new KCUserInfoHandler());
        server.createContext("/api/develop", new KCDevelopHandler()).getFilters().add(new ParameterFilter());
        server.createContext("/api/getfleetorg", new KCFleetOrgReqHandler());
        server.createContext("/api/sparkle", new KCSparkleHandler()).getFilters().add(new ParameterFilter());
        server.createContext("/api/bdock", new KCBuildDocksHandler());
        server.createContext("/api/construct", new KCConstructHandler()).getFilters().add(new ParameterFilter());
        server.createContext("/api/battle15", new KC15Handler()).getFilters().add(new ParameterFilter());
        server.createContext("/api/resfarm", new KC15Handler.KCResFarmHandler()).getFilters().add(new ParameterFilter());
        server.createContext("/api/kcidprepare", new KC15Handler.KCPrepareScapHandler());
        server.createContext("/api/kcidinit", new KCIDInitHandler()).getFilters().add(new ParameterFilter());
        server.createContext("/api/port", new KCPortHandler()).getFilters().add(new ParameterFilter());
        server.createContext("/api/e2u", new KCE2UHandler()).getFilters().add(new ParameterFilter());
        server.createContext("/api/droplog", new KCDropLogHandler());
        server.createContext("/api/buildlog", new KCBuildLogHandler());


        delegate.createContexts(server);

        // Delegate Server

        // TODO Create API Req Contexts
    }

    // Command Receivers
    /*
    // Template
    static class KCHandler implements HttpHandler {
        @Override
        public void handle(HttpExchange exchange) throws IOException {
            Map params = (Map)exchange.getAttribute("parameters");
            JSONObject response = new JSONObject();

            // TODO Process Here

            exchange.getResponseHeaders().set("Content-Type","text/json;charset=utf-8");
            exchange.sendResponseHeaders(200, response.toString().getBytes(Charset.forName("utf-8")).length);
            OutputStream os = exchange.getResponseBody();
            os.write(response.toString().getBytes(Charset.forName("utf-8")));
            os.close();
        }
    }
    */


    private static InitThread initThread;
    static class KCInitHandler implements HttpHandler {
        @Override
        public void handle(HttpExchange exchange) throws IOException {
            Map params = (Map)exchange.getAttribute("parameters");
            JSONObject response = new JSONObject();

            if (!(new File("shipsdb.json").exists())) {
                System.out.println("Initializing shipsdb...");
                initThread = new InitThread((String) params.get("url"));
                initThread.start();
                response.put("status","initdb");
            } else {
                try {
                    use_kcid = Boolean.parseBoolean((String)params.get("use_kcid"));
                    session = new Session((String) params.get("url"));
                    delegate = new KCDelegateServer(session, kcid);
                    response.put("status", "success");
                    JSONObject data = new JSONObject();
                    data.put("user", session.username);
                    if (!use_kcid) {
                        init_done = true;
                    } else {
                        URL session_url = new URL(session.url);

                        Desktop.getDesktop().browse(new URI(session_url.toString().replace(session_url.getHost(),"localhost")));
                    }
                    response.put("data", data);
                    response.put("status","success");
                } catch (Exception e) {
                    response.put("status", "failure");
                    response.put("reason", e.toString());
                }
            }
            exchange.getResponseHeaders().set("Content-Type", "text/json;charset=utf-8");
            exchange.sendResponseHeaders(200, response.toString().getBytes(Charset.forName("utf-8")).length);
            OutputStream os = exchange.getResponseBody();
            os.write(response.toString().getBytes(Charset.forName("utf-8")));
            os.close();
        }
    }

    static class KCDBLoadHandler implements HttpHandler {
        @Override
        public void handle(HttpExchange exchange) throws IOException {
            JSONObject response = new JSONObject();

            if (initThread.done) {
                session = initThread.session;
                delegate = new KCDelegateServer(session, kcid);
                response.put("status", "success");
                JSONObject data = new JSONObject();
                data.put("user", session.username);
                if (!use_kcid) {
                    init_done = true;
                } else {
                    try {
                        Desktop.getDesktop().browse(new URI(session.url));
                    } catch (Exception e){}
                }
                response.put("data", data);
                response.put("status","success");
            } else if (initThread.error) {
                response.put("status", "failure");
                response.put("reason", initThread.exception.toString());
            } else {
                response.put("status","initdb");
            }

            exchange.getResponseHeaders().set("Content-Type", "text/json;charset=utf-8");
            exchange.sendResponseHeaders(200, response.toString().getBytes(Charset.forName("utf-8")).length);
            OutputStream os = exchange.getResponseBody();
            os.write(response.toString().getBytes(Charset.forName("utf-8")));
            os.close();
        }
    }



    static class KCUserInfoHandler implements HttpHandler {
        @Override
        public void handle(HttpExchange exchange) throws IOException {
            JSONObject response = new JSONObject();
            response.put("status","success");
            response.put("data",session.user);
            if (portno==80) {
                response.put("serveraddr", InetAddress.getLocalHost().getHostAddress());
            } else {
                response.put("serveraddr", InetAddress.getLocalHost().getHostAddress() + ":" + portno);
            }

            // Get local svr
            URL localsvr = new URL(session.url);
            response.put("localsvr", localsvr.getFile());
            //System.out.(session.user);
            exchange.getResponseHeaders().set("Content-Type","text/json;charset=utf-8");
            exchange.sendResponseHeaders(200, response.toString().getBytes(Charset.forName("utf-8")).length);
            OutputStream os = exchange.getResponseBody();
            os.write(response.toString().getBytes(Charset.forName("utf-8")));
            os.close();
        }
    }

    static class KCDevelopHandler implements HttpHandler {
        @Override
        public void handle(HttpExchange exchange) throws IOException {
            Map params = (Map)exchange.getAttribute("parameters");
            JSONObject response = new JSONObject();
            try {
                int count = Integer.parseInt((String)params.get("count"));
                int fuel = Integer.parseInt((String)params.get("fuel"));
                int ammo = Integer.parseInt((String)params.get("ammo"));
                int steel = Integer.parseInt((String)params.get("steel"));
                int baux = Integer.parseInt((String)params.get("baux"));
                String result = "";
                for (int i=0; i<count; i++) {
                    result += session.arsenal.develop(fuel, ammo, steel, baux)+"<br /><br />\n";
                }
                response.put("status","success");
                response.put("data",result);
            } catch (Exception e) {
                response.put("status","failure");
                response.put("reason",e.toString());
            }

            exchange.getResponseHeaders().set("Content-Type", "text/json;charset=utf-8");
            exchange.sendResponseHeaders(200, response.toString().getBytes(Charset.forName("utf-8")).length);
            OutputStream os = exchange.getResponseBody();
            os.write(response.toString().getBytes(Charset.forName("utf-8")));
            os.close();
        }
    }


    static class KCFleetOrgReqHandler implements HttpHandler {
        @Override
        public void handle(HttpExchange exchange) throws IOException {
            JSONObject response = new JSONObject();

            try {
                session.fleet.update();

                Deck deck = new Deck(session, 1);

                // Error handling: when the user have fleets locked, GetDeckRequest throws errors
                // however, the first fleet is always present,
                // so the request for the first fleet throws and ONLY throws exceptions related to connection
                JSONArray decks = new JSONArray();

                decks.put(deck2json(deck));

                // If any connection errors occured, the error is thrown and fed to the user in the catch block

                for (int i=2; i<=4; i++) {
                    // Now the connection has established correctly
                    // Ignore any indexOutOfBound error caused by trying to access locked fleet
                    // As no other exceptions can be thrown at this moment
                    try {
                        deck.setDeck(i);
                        decks.put(deck2json(deck));
                    } catch (Exception e) {
                        break;
                    }
                }
                response.put("data", decks);
                response.put("status", "success");
            } catch (Exception e) {
                response.put("status","failure");
                response.put("reason",e.toString());
            }

            exchange.getResponseHeaders().set("Content-Type","text/json;charset=utf-8");
            exchange.sendResponseHeaders(200, response.toString().getBytes(Charset.forName("utf-8")).length);
            OutputStream os = exchange.getResponseBody();
            os.write(response.toString().getBytes(Charset.forName("utf-8")));
            os.close();
        }

        private JSONObject deck2json(Deck deck) {
            JSONObject json = new JSONObject();
            json.put("name",deck.toString());
            JSONArray arr = new JSONArray();
            for (Ship ship : deck.getShips()) {
                arr.put(ship2json(ship));
            }
            json.put("ships",arr);
            return json;
        }

        private JSONObject ship2json(Ship ship) {
            JSONObject json = new JSONObject();
            json.put("name",ship.description);
            json.put("lv", ship.getLv());
            json.put("hp", ship.getHp());
            json.put("maxhp", ship.getMaxhp());
            json.put("condition", ship.getCondition());
            return json;
        }
    }


    static class KCSparkleHandler implements HttpHandler {
        private static int deckno, shipno, map;
        private static int[] originalID;
        private static Sparkle spObj;
        private Ship objShip;
        @Override
        public void handle(HttpExchange exchange) throws IOException {
            Map params = (Map)exchange.getAttribute("parameters");
            JSONObject response = new JSONObject();

            switch ((String)params.get("action")) {
                case "begin":
                    try {
                        deckno = Integer.parseInt((String)params.get("deckno"));
                        shipno = Integer.parseInt((String)params.get("shipno"))-1;
                        map = Integer.parseInt((String)params.get("maparea"));
                        Deck activeDeck = new Deck(session, deckno);
                        spObj = new Sparkle(session);
                        if (map!=1&&map!=5) {
                            throw new Exception("Maparea only support 1-1 and 1-5");
                        }
                        spObj.prepare();
                        objShip = activeDeck.getShip(shipno);
                        java.util.List<Ship> activeShips = activeDeck.getShips();
                        originalID = new int[6];
                        for (int i = 0; i < 6; i++) {
                            originalID[i] = -1;
                        }
                        for (int i=0; i<activeShips.size(); i++) {
                            originalID[i] = activeShips.get(i).getId();
                        }
                        // Switch to ship
                        new OrganizationRequest(session.url, deckno, 0, objShip.getId()).request();
                        for (int i=5; i>0; i--) {
                            new OrganizationRequest(session.url, deckno, i, -1).request();
                        }
                        response.put("ship", objShip.toString() + "@1-" + map);
                        response.put("status", "success");
                    } catch(Exception e) {
                        response.put("status","failure");
                        response.put("reason",e.toString());
                    }
                    break;
                case "auxship":
                    try {
                        List<String> activeFleet = new ArrayList<String>();
                        String addedShipNames = "";
                        activeFleet.add(objShip.description);
                        for (Ship ship : session.fleet.getShips()) {
                            if (activeFleet.size() <= 5 && !ship.isLocked() && session.db.getShipType(ship.getShip()).equals("DD")) {
                                boolean accept = true;
                                for (String shipName : activeFleet) {
                                    if (shipName.equals(ship.description)) {
                                        accept = false;
                                    }
                                }
                                if (accept) {
                                    activeFleet.add(ship.description);
                                    addedShipNames += ship.description + " ";
                                    OrganizationRequest req = new OrganizationRequest(session.url, deckno, activeFleet.size()-1, ship.getId());
                                    req.request();
                                }
                            }
                        }
                        response.put("auxships",addedShipNames);
                        response.put("status","success");
                    } catch (Exception e) {
                        response.put("status","failure");
                        response.put("reason",e.toString());
                    }
                    break;
                case "battle":
                    try {
                        spObj.begin(deckno, ""+map);
                        Thread.sleep(1000);
                        spObj.battle();
                        Thread.sleep(5000);
                        JSONObject data = spObj.battleResult();
                        response.put("result", data.getString("api_win_rank") + data.getInt("api_mvp") + "  ");
                        if (objShip.getHp() <= objShip.getMaxhp() * 0.50) {
                            throw new Exception("船只严重受损,请修理!");
                        }
                        response.put("status", "success");
                    } catch (Exception e) {
                        response.put("status","failure");
                        response.put("reason",e.toString());
                    }
                    break;
                case "resupply":
                    try {
                        spObj.resupply(deckno);
                        response.put("status", "success");
                    } catch (Exception e) {
                        response.put("status","failure");
                        response.put("reason",e.toString());
                    }
                    break;
                case "restoreOrg":
                    try {
                        for (int i = 0; i < 6; i++) {
                            new OrganizationRequest(session.url, deckno, i, originalID[i]).request();
                        }
                        response.put("status", "success");
                    } catch (Exception e) {
                        response.put("status","failure");
                        response.put("reason",e.toString());
                    }
                    break;
            }

            exchange.getResponseHeaders().set("Content-Type", "text/json;charset=utf-8");
            exchange.sendResponseHeaders(200, response.toString().getBytes(Charset.forName("utf-8")).length);
            OutputStream os = exchange.getResponseBody();
            os.write(response.toString().getBytes(Charset.forName("utf-8")));
            os.close();
        }
    }

    static class KCBuildDocksHandler implements HttpHandler {
        @Override
        public void handle(HttpExchange exchange) throws IOException {
            JSONObject response = new JSONObject();

            try {
                CustomRequest retrieve = new CustomRequest(session.url, "api_get_member/kdock", "");
                JSONObject docks = retrieve.request();
                JSONArray data = docks.getJSONArray("api_data");
                response.put("data",data);
                JSONArray ship_names = new JSONArray();
                for (int i=0; i<data.length(); i++) {
                    int shipID = data.getJSONObject(i).getInt("api_created_ship_id");
                    if (shipID!=0) {
                        ship_names.put(session.db.getShipName(shipID));
                    } else {
                        ship_names.put("-");
                    }
                }
                response.put("ship_name",ship_names);
                response.put("status", "success");
            } catch (Exception e) {
                response.put("status","failure");
                response.put("reason",e.toString());
            }

            exchange.getResponseHeaders().set("Content-Type","text/json;charset=utf-8");
            exchange.sendResponseHeaders(200, response.toString().getBytes(Charset.forName("utf-8")).length);
            OutputStream os = exchange.getResponseBody();
            os.write(response.toString().getBytes(Charset.forName("utf-8")));
            os.close();
        }
    }

    static class KCConstructHandler implements HttpHandler {
        @Override
        public void handle(HttpExchange exchange) throws IOException {
            Map params = (Map)exchange.getAttribute("parameters");
            JSONObject response = new JSONObject();

            try {
                int slot = Integer.parseInt((String) params.get("slot"));
                int fuel = Integer.parseInt((String) params.get("fuel"));
                int ammo = Integer.parseInt((String) params.get("ammo"));
                int steel = Integer.parseInt((String)params.get("steel"));
                int baux = Integer.parseInt((String)params.get("baux"));
                int seaweed = Integer.parseInt((String)params.get("seaweed"));
                int count = Integer.parseInt((String)params.get("count"));
                String result = "";
                if (Boolean.parseBoolean((String) params.get("lsc"))) {
                    if (Boolean.parseBoolean((String)params.get("fire"))) {
                        for (int i=0; i<count; i++) {
                            result += session.arsenal.constructLargeInsta(slot, fuel, ammo, steel, baux, seaweed)+"<br /><br />\n";
                        }
                    } else {
                        session.arsenal.constructLarge(slot, fuel, ammo, steel, baux, seaweed);
                    }
                } else {
                    if (Boolean.parseBoolean((String)params.get("fire"))) {
                        for (int i=0; i<count; i++) {
                            result += session.arsenal.buildinstant(slot, fuel, ammo, steel, baux, 0.0) +"<br /><br />\n";
                        }
                    } else {
                        session.arsenal.build(slot, fuel, ammo, steel, baux);
                    }
                }
                response.put("data",result);
                response.put("status","success");
            } catch (Exception e) {
                response.put("status","failure");
                response.put("reason",e.toString());
            }

            exchange.getResponseHeaders().set("Content-Type", "text/json;charset=utf-8");
            exchange.sendResponseHeaders(200, response.toString().getBytes(Charset.forName("utf-8")).length);
            OutputStream os = exchange.getResponseBody();
            os.write(response.toString().getBytes(Charset.forName("utf-8")));
            os.close();
        }
    }

    static class KCShutdownHandler implements HttpHandler {
        @Override
        public void handle(HttpExchange exchange) throws IOException {
            JSONObject response = new JSONObject();
            response.put("status", "success");
            exchange.getResponseHeaders().set("Content-Type", "text/json;charset=utf-8");
            exchange.sendResponseHeaders(200, response.toString().getBytes(Charset.forName("utf-8")).length);
            OutputStream os = exchange.getResponseBody();
            os.write(response.toString().getBytes(Charset.forName("utf-8")));
            os.close();
            server.stop(0);
        }
    }

    static class KC15Handler implements HttpHandler {
        private static int deckno, map_major, map_minor, hp_threshold, cond_threshold;
        private static Battle15 battle;
        @Override
        public void handle(HttpExchange exchange) throws IOException {
            Map params = (Map)exchange.getAttribute("parameters");
            JSONObject response = new JSONObject();

            switch ((String)params.get("action")) {
                case "begin":
                    try {
                        deckno = Integer.parseInt((String) params.get("deck"));
                        map_major = Integer.parseInt((String)params.get("map_major"));
                        map_minor = Integer.parseInt((String)params.get("map_minor"));
                        hp_threshold = Integer.parseInt((String)params.get("hp_threshold"));
                        cond_threshold = Integer.parseInt((String)params.get("cond_threshold"));

                        battle = new Battle15(session, deckno);
                        session.fleet.update();
                        battle = new Battle15(session, deckno);
                        response.put("status", "success");
                    } catch (Exception e) {
                        response.put("status","failure");
                        response.put("reason", e.toString());
                    }
                    break;
                case "battle":
                    try {
                        Deck deck = new Deck(session, deckno);
                        for (Ship ship : deck.getShips()) {
                            if (ship.getCondition()<cond_threshold) {
                                throw new Exception("船只红脸.");
                            }
                            if (ship.getHp()<ship.getMaxhp()*hp_threshold/100) {
                                throw new Exception("船只破损.");
                            }
                        }

                        battle.begin(map_major, map_minor);
                        Thread.sleep(2000);

                        battle.battle();
                        Thread.sleep(5000);

                        JSONObject data = battle.battleResult();

                        String result = "";
                        result+="Battle Result: " + data.getString("api_quest_name") + " " + data.getJSONObject("api_enemy_info").getString("api_deck_name")+"<br />\n";
                        result+="<hr />\n";
                        result+="Rank: " + data.getString("api_win_rank")+"<br />\n";
                        result+=" MVP#" + data.getInt("api_mvp") + "  基础经验:" + data.getInt("api_get_base_exp")+"<br />\n<br />\n";
                        int i = 0;
                        for (Ship ship : deck.getShips()) {
                            i++;
                            result+="" + deck.getShip(i - 1) + " EXP+" + data.getJSONArray("api_get_ship_exp").getInt(i); // Data returned indexes from 1 to 6
                            result+=" (" + (data.getJSONArray("api_get_exp_lvup").getJSONArray(i - 1).getInt(0) - data.getJSONArray("api_get_exp_lvup").getJSONArray(i - 1).getInt(1)) + ")";
                            result+=" Lv." + ship.getLv();
                            result+=" HP: " + ship.getHp() + "/" + ship.getMaxhp()+"<br />\n";

                        }
                        result+="<br />\n";
                        if (data.getJSONArray("api_get_flag").getInt(1) != 0) {
                            // Get new ship
                            result+="+ 发现新船只: " + data.getJSONObject("api_get_ship").getString("api_ship_name");
                        }
                        response.put("status","success");
                        response.put("result",result);
                    } catch (Exception e) {
                        response.put("status","failure");
                        response.put("reason",e.toString());
                    }
                    break;
                case "resupply":
                    try {
                        battle.resupply();
                        response.put("status","success");
                    } catch (Exception e) {
                        response.put("status","failure");
                        response.put("reason",e.toString());
                    }
                    break;
            }

            exchange.getResponseHeaders().set("Content-Type", "text/json;charset=utf-8");
            exchange.sendResponseHeaders(200, response.toString().getBytes(Charset.forName("utf-8")).length);
            OutputStream os = exchange.getResponseBody();
            os.write(response.toString().getBytes(Charset.forName("utf-8")));
            os.close();
        }

        static class KCResFarmHandler implements HttpHandler {
            @Override
            public void handle(HttpExchange exchange) throws IOException {
                Map params = (Map)exchange.getAttribute("parameters");
                JSONObject response = new JSONObject();

                try {
                    int map_major = Integer.parseInt((String)params.get("map_major"));
                    int map_minor = Integer.parseInt((String)params.get("map_minor"));
                    int deck = Integer.parseInt((String)params.get("deck"));
                    ResourceFarming farm = new ResourceFarming(session);
                    JSONArray result = new JSONArray(farm.farm(map_major, map_minor, deck));
                    response.put("result",result);
                    response.put("status","success");
                } catch (Exception e) {
                    response.put("status","failure");
                    response.put("reason",e.toString());
                }

                exchange.getResponseHeaders().set("Content-Type","text/json;charset=utf-8");
                exchange.sendResponseHeaders(200, response.toString().getBytes(Charset.forName("utf-8")).length);
                OutputStream os = exchange.getResponseBody();
                os.write(response.toString().getBytes(Charset.forName("utf-8")));
                os.close();
            }
        }

        static class KCPrepareScapHandler implements HttpHandler {
            @Override
            public void handle(HttpExchange exchange) throws IOException {
                JSONObject response = new JSONObject();

                try {
                    Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
                    kcid = new InputDaemon();
                    delegate.setKcid(kcid);
                    kcid.setBound(0,0,(int)screenSize.getWidth(),(int)screenSize.getHeight());
                    kcid.screenCap();
                    response.put("screenw",screenSize.getWidth());
                    response.put("screenh",screenSize.getHeight());
                    response.put("status","success");
                } catch (Exception e) {
                    response.put("status","failure");
                    response.put("reason",e.toString());
                }

                exchange.getResponseHeaders().set("Content-Type","text/json;charset=utf-8");
                exchange.sendResponseHeaders(200, response.toString().getBytes(Charset.forName("utf-8")).length);
                OutputStream os = exchange.getResponseBody();
                os.write(response.toString().getBytes(Charset.forName("utf-8")));
                os.close();
            }
        }



    }

    static class KCPortHandler implements HttpHandler {
        @Override
        public void handle(HttpExchange exchange) throws IOException {
            Map params = (Map)exchange.getAttribute("parameters");
            JSONObject response = new JSONObject();

            if (kcid==null) {
                response.put("status","not_supported");
            } else {
                try {
                    String action = (String) params.get("action");
                    switch (action) {
                        case "request":
                            kcid.port(session.url);
                            response.put("status", "success");
                            break;
                        case "wait":
                            response.put("finished", kcid.port_finished);
                            response.put("status", "success");
                            break;
                    }
                } catch (Exception e) {
                    response.put("status", "failure");
                    response.put("reason", e.toString());
                }
            }

            exchange.getResponseHeaders().set("Content-Type","text/json;charset=utf-8");
            exchange.sendResponseHeaders(200, response.toString().getBytes(Charset.forName("utf-8")).length);
            OutputStream os = exchange.getResponseBody();
            os.write(response.toString().getBytes(Charset.forName("utf-8")));
            os.close();
        }
    }

    static class KCIDInitHandler implements HttpHandler {
        @Override
        public void handle(HttpExchange exchange) throws IOException {
            Map params = (Map)exchange.getAttribute("parameters");
            JSONObject response = new JSONObject();

            try {
                int     x0 = Integer.parseInt((String)params.get("x0")),
                        y0 = Integer.parseInt((String) params.get("y0")),
                        x1 = Integer.parseInt((String) params.get("x1")),
                        y1 = Integer.parseInt((String) params.get("y1"));

                kcid.setBound(x0, y0, x1, y1);
                init_done = true;
                response.put("status","success");
            } catch (Exception e) {
                response.put("status","failure");
                response.put("reason",e.toString());
            }

            exchange.getResponseHeaders().set("Content-Type","text/json;charset=utf-8");
            exchange.sendResponseHeaders(200, response.toString().getBytes(Charset.forName("utf-8")).length);
            OutputStream os = exchange.getResponseBody();
            os.write(response.toString().getBytes(Charset.forName("utf-8")));
            os.close();
        }
    }

    private static E2Unryuu e2u;
    static class KCE2UHandler implements HttpHandler {
        @Override
        public void handle(HttpExchange exchange) throws IOException {
            Map params = (Map)exchange.getAttribute("parameters");
            JSONObject response = new JSONObject();

            if (e2u==null) {
                e2u = new E2Unryuu(session);
            }

            try {
                String action = (String) params.get("action");
                switch (action) {
                    case "sortie":
                        String ship = e2u.battle();
                        response.put("loot",ship);
                        response.put("status","success");
                        break;
                    case "fixes":
                        int time = e2u.fixes();
                        response.put("wait",time);
                        response.put("status","success");
                        break;
                }
            } catch (Exception e) {
                response.put("status","failure");
                response.put("reason",e.toString());
                e.printStackTrace();
            }

            exchange.getResponseHeaders().set("Content-Type","text/json;charset=utf-8");
            exchange.sendResponseHeaders(200, response.toString().getBytes(Charset.forName("utf-8")).length);
            OutputStream os = exchange.getResponseBody();
            os.write(response.toString().getBytes(Charset.forName("utf-8")));
            os.close();
        }
    }

    static class KCDropLogHandler implements HttpHandler {
        @Override
        public void handle(HttpExchange exchange) throws IOException {
            JSONObject response = new JSONObject();

            for (KCLogger logger : session.loggers) {
                if (logger.getClass().equals(DropLogging.class)) {
                    ((DropLogging)logger).exportCSV();
                }
            }

            response.put("status","success");

            exchange.getResponseHeaders().set("Content-Type","text/json;charset=utf-8");
            exchange.sendResponseHeaders(200, response.toString().getBytes(Charset.forName("utf-8")).length);
            OutputStream os = exchange.getResponseBody();
            os.write(response.toString().getBytes(Charset.forName("utf-8")));
            os.close();
        }
    }

    static class KCBuildLogHandler implements HttpHandler {
        @Override
        public void handle(HttpExchange exchange) throws IOException {
            JSONObject response = new JSONObject();

            for (KCLogger logger : session.loggers) {
                if (logger.getClass().equals(BuildLogging.class)) {
                    ((BuildLogging)logger).exportCSV();
                }
            }

            response.put("status","success");

            exchange.getResponseHeaders().set("Content-Type","text/json;charset=utf-8");
            exchange.sendResponseHeaders(200, response.toString().getBytes(Charset.forName("utf-8")).length);
            OutputStream os = exchange.getResponseBody();
            os.write(response.toString().getBytes(Charset.forName("utf-8")));
            os.close();
        }
    }


    // TODO Append more processor classes here

}
