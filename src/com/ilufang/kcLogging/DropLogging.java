package com.ilufang.kcLogging;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.nio.charset.Charset;
import java.util.Date;

/**
 * Created by root on 5/13/15.
 */
public class DropLogging extends KCLogging {
    public DropLogging() throws IOException {
        logfile = "loots.json";
        loadLog();
    }

    private int map_major, map_minor, map_node;

    @Override
    public void processRequest(String url, String request, String response)  {
        if (url.startsWith("/kcsapi/api_req_map/next")||url.startsWith("/kcsapi/api_req_map/start")){
            // Record node encounter
            JSONObject data = new JSONObject(response.substring(7)).getJSONObject("api_data");
            map_major = data.getInt("api_maparea_id");
            map_minor = data.getInt("api_mapinfo_no");
            map_node = data.getInt("api_no");
        } else if (url.startsWith("/kcsapi/api_req_sortie/battleresult")||url.startsWith("/kcsapi/api_req_combined_battle/battleresult")) {
            // Parse Loot
            JSONObject data = new JSONObject(response.substring(7)).getJSONObject("api_data");
            if (data.getJSONArray("api_get_flag").getInt(1)!=0) {
                // Get new ship
                String ship = data.getJSONObject("api_get_ship").getString("api_ship_name");
                String rank = data.getString("api_win_rank");
                log.append("log",encodeJSONLoot(ship, rank));
            } else {
                String rank = data.getString("api_win_rank");
                log.append("log",encodeJSONLoot("-",rank));
            }
            try {
                saveLog();
            } catch (Exception e){
                System.out.println("Warning: Could not save log.");
                e.printStackTrace();
            }
        }

    }

    private JSONObject encodeJSONLoot(String shipname, String rank) {
        JSONObject obj = new JSONObject();
        obj.put("map_major",map_major);
        obj.put("map_minor",map_minor);
        obj.put("map_node",map_node);
        obj.put("loot", shipname);
        obj.put("rank", rank);
        obj.put("date",new Date().toString());
//        System.out.println("Recorded:"+map_major+"-"+map_minor+(char)(map_node+'A'-1)+" ("+rank+") :"+shipname);
        return obj;
    }

    public void addEntry(int map_major, int map_minor, int map_node, String ship, String rank) {
        JSONObject obj = new JSONObject();
        obj.put("map_major",map_major);
        obj.put("map_minor",map_minor);
        obj.put("map_node",map_node);
        obj.put("loot",ship);
        obj.put("rank",rank);
        obj.put("date",new Date().toString());
        log.append("log",obj);
        try {
            saveLog();
        } catch (Exception e){
            System.out.println("Warning: Could not save log.");
            e.printStackTrace();
        }

    }

    public void exportCSV() throws IOException {
        File csv = new File("loots.csv");
        csv.delete();
        csv.createNewFile();
        OutputStream os = new FileOutputStream(csv);
        os.write("Date,Node,Rank,Loot\n".getBytes(Charset.forName("utf-8")));
        JSONArray entries = log.getJSONArray("log");
        for (int i=0; i<entries.length(); i++) {
            JSONObject entry = entries.getJSONObject(i);
            String line = entry.getString("date");
            line += "," + entry.getInt("map_major") + '-' + entry.getInt("map_minor") + ' ' + (char)(entry.getInt("map_node")+'A'-1) + ',';
            line += entry.getString("rank") + ',';
            line += entry.getString("loot") + '\n';
            os.write(line.getBytes(Charset.forName("utf-8")));
        }
        os.close();
    }

}
