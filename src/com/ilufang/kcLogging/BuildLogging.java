package com.ilufang.kcLogging;

import com.ilufang.kcUtils.Session;
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
public class BuildLogging extends KCLogging {
    Session session;

    public BuildLogging(Session session) throws IOException {
        this.session = session;
        building = false;
        logfile = "build.json";
        loadLog();
    }

    private int fuel, ammo, steel, baux, seaweed, slot;
    private boolean building;

    @Override
    public void processRequest(String url, String request, String response)  {
        if (url.startsWith("/kcsapi/api_req_kousyou/createship")||url.startsWith("/kcsapi/api_req_kousyou/createitem")){
            // Record Construction/Development prompt
            request = request.replace("%5F","_");
            String[] args = request.split("&");
            for (String arg : args) {
                String[] parts = arg.split("=");
                String k = parts[0], v = parts[1];
                switch (k) {
                    case "api_kdock_id":
                        slot = Integer.parseInt(v);
                        break;
                    case "api_item1":
                        fuel = Integer.parseInt(v);
                        break;
                    case "api_item2":
                        ammo = Integer.parseInt(v);
                        break;
                    case "api_item3":
                        steel = Integer.parseInt(v);
                        break;
                    case "api_item4":
                        baux = Integer.parseInt(v);
                        break;
                    case "api_item5":
                        seaweed = Integer.parseInt(v);
                        break;
                }
            }
            if (url.startsWith("/kcsapi/api_req_kousyou/createitem")) {
                // Instantly record result, for development
                JSONObject entry = new JSONObject();
                entry.put("type","Development");
                entry.put("date",new Date().toString());
                entry.put("fuel", fuel);
                entry.put("ammo", ammo);
                entry.put("steel", steel);
                entry.put("baux", baux);
                entry.put("seaweed", 1); // No corresponding data about seaweed
                JSONObject data = new JSONObject(response.substring(7));
                if (data.getJSONObject("api_data").getInt("api_create_flag")!=1) {
                    // failure
                    entry.put("product","Failure");
                } else {
                    int itemid = data.getJSONObject("api_data").getJSONObject("api_slot_item").getInt("api_slotitem_id");
                    entry.put("product", session.db.getEquipName(itemid));
                }
                log.append("log",entry);
                try {
                    saveLog();
                } catch (IOException e) {
                    System.out.println("Warning: Could not save log.");
                    e.printStackTrace();
                }
            } else {
                // Constructing ship, enable kdock listening
                building = true;
            }

        } else if (url.startsWith("/kcsapi/api_get_member/kdock")) {
            if (building) {
                // Ignore non-construction kdock requests (eg. startup query)
                building = false;
                // Parse Result
                JSONObject data = new JSONObject(response.substring(7));
                JSONObject dock = data.getJSONArray("api_data").getJSONObject(slot - 1);
                int ship = dock.getInt("api_created_ship_id");
                JSONObject entry = new JSONObject();
                entry.put("type", "Construction");
                entry.put("date", new Date().toString());
                entry.put("fuel", fuel);
                entry.put("ammo", ammo);
                entry.put("steel", steel);
                entry.put("baux", baux);
                entry.put("seaweed", seaweed);
                entry.put("product", session.db.getShipName(ship));
                log.append("log", entry);
                try {
                    saveLog();
                } catch (IOException e) {
                    System.out.println("Warning: Could not save log.");
                    e.printStackTrace();
                }
            }
        }

    }

    public void exportCSV() throws IOException {
        File csv = new File("build.csv");
        csv.delete();
        csv.createNewFile();
        OutputStream os = new FileOutputStream(csv);
        os.write("Date,Type,Fuel,Ammo,Steel,Baux,Material,Product\n".getBytes(Charset.forName("utf-8")));
        JSONArray entries = log.getJSONArray("log");
        for (int i=0; i<entries.length(); i++) {
            JSONObject entry = entries.getJSONObject(i);
            String line = entry.getString("date");
            line += "," + entry.getString("type");
            line += "," + entry.getInt("fuel");
            line += "," + entry.getInt("ammo");
            line += "," + entry.getInt("steel");
            line += "," + entry.getInt("baux");
            line += "," + entry.getInt("seaweed");
            line += "," + entry.getString("product") + '\n';
            os.write(line.getBytes(Charset.forName("utf-8")));
        }
        os.close();
    }


}
