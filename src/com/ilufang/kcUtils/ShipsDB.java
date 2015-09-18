package com.ilufang.kcUtils;

import com.ilufang.kcRequest.CustomRequest;
import org.json.*;

import java.io.*;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by ilufang on 4/20/15.
 */
public class ShipsDB {
    private List<JSONObject> ships;
    private List<JSONObject> equipments;
    Session session;


    public static String[] shipTypes;

    {
        shipTypes = new String[22];
        shipTypes[2]="DD";
        shipTypes[3]="CL";
        shipTypes[4]="CLT";
        shipTypes[5]="CA";
        shipTypes[6]="CAV";
        shipTypes[7]="CVL";
        shipTypes[8]="FBB";
        shipTypes[9]="BB";
        shipTypes[10]="BBV";
        shipTypes[11]="CV";
        shipTypes[13]="SS";
        shipTypes[14]="SSV";
        shipTypes[15]="Transport"; // Abyssal only
        shipTypes[16]="AV";
        shipTypes[17]="LHA"; // 扬陆舰
        shipTypes[18]="CV-Armor";
        shipTypes[19]="AR"; // Akashi
        shipTypes[20]="AS"; // Taigei
        shipTypes[21]="CLp"; // CL-Training
    }


    public ShipsDB(Session session) throws Exception {
        this.session = session;
        this.ships = new ArrayList<JSONObject>();
        this.equipments = new ArrayList<JSONObject>();
        JSONObject data = getDB();
        JSONArray ships = data.getJSONArray("api_mst_ship");
        for (int i=ships.length()-1; i>=0; i--) {
            this.ships.add(ships.getJSONObject(i));
            /*
            Ships:
            api_id = index
            api_name = description
            api_fuel_max
            api_bull_max
            api_yomi = pronunciationmark / elite.flagship
             */
        }
        JSONArray equips = data.getJSONArray("api_mst_slotitem");
        for (int i=equips.length()-1; i>=0; i--) {
            this.equipments.add(equips.getJSONObject(i));
            /*
            Equips:
            api_id = index
            api_name = description
             */
        }

    }

    private JSONObject getDB() throws Exception {
        File file=new File("shipsdb.json");
        if(file.exists()) {
            // read locally
            FileInputStream fis=new FileInputStream(file);
            InputStreamReader isr = new InputStreamReader(fis, Charset.forName("utf-8"));
            BufferedReader reader = new BufferedReader(isr);
            String data = "", tmp;

            while((tmp=reader.readLine())!=null) {
                data += tmp;
            }
            return new JSONObject(data).getJSONObject("api_data");
        }
        String data = update();
        if (data.length()!=0) {
            file.createNewFile();
            FileOutputStream out=new FileOutputStream(file,true);
            out.write(data.getBytes(Charset.forName("utf-8")));
            out.close();
        }
        return new JSONObject(data).getJSONObject("api_data");
    }

    public void exportCSV() throws Exception {
        JSONObject data = getDB();
        JSONArray ships = data.getJSONArray("api_mst_ship");
        File csv = new File("ships.csv");
        csv.delete();
        csv.createNewFile();
        FileOutputStream os = new FileOutputStream(csv);
        os.write("ID,Name,Yomi,图鉴ID,类型,HP,hp1,火力,fp1,雷装,tp1,对空,aa1,装甲,ar1,运,luck1,装备槽,搭载1,搭载2,搭载3,搭载4,耗油,耗弹,建造时间,改造产物ID,改造等级,改造耗弹,改造耗钢,VOICEF,BACKS,SOKU,捞船介绍\n".getBytes(Charset.forName("utf-8")));

        for (int i=ships.length()-1; i>=0; i--) {
            JSONObject ship = ships.getJSONObject(i);
            os.write(String.format("%d,%s,%s,%d,%s,%d,%d,%d,%d,%d,%d,%d,%d,%d,%d,%d,%d,%d,%d,%d,%d,%d,%d,%d,%d,%d,%d,%d,%d,%d,%d,%d,%s\n",
                ship.getInt("api_id"),
                ship.getString("api_name"),
                ship.getString("api_yomi"),
                ship.getInt("api_sortno"),
                shipTypes[ship.getInt("api_stype")],
                ship.getJSONArray("api_taik").getInt(0),
                ship.getJSONArray("api_taik").getInt(1),
                ship.getJSONArray("api_houg").getInt(0),
                ship.getJSONArray("api_houg").getInt(1),
                ship.getJSONArray("api_raig").getInt(0),
                ship.getJSONArray("api_raig").getInt(1),
                ship.getJSONArray("api_tyku").getInt(0),
                ship.getJSONArray("api_tyku").getInt(1),
                ship.getJSONArray("api_souk").getInt(0),
                ship.getJSONArray("api_souk").getInt(1),
                ship.getJSONArray("api_luck").getInt(0),
                ship.getJSONArray("api_luck").getInt(1),
                ship.getInt("api_slot_num"),
                ship.getJSONArray("api_maxeq").getInt(0),
                ship.getJSONArray("api_maxeq").getInt(1),
                ship.getJSONArray("api_maxeq").getInt(2),
                ship.getJSONArray("api_maxeq").getInt(3),
                ship.getInt("api_fuel_max"),
                ship.getInt("api_bull_max"),
                ship.getInt("api_buildtime"),
                ship.getInt("api_aftershipid"),
                ship.getInt("api_afterlv"),
                ship.getInt("api_afterbull"),
                ship.getInt("api_afterfuel"),
                ship.getInt("api_voicef"),
                ship.getInt("api_backs"),
                ship.getInt("api_soku"),
                ship.getString("api_getmes")
            ).getBytes(Charset.forName("utf-8")));
        }

    }

    public String update() throws Exception {
        CustomRequest req = new CustomRequest(session.url, "api_start2", "");
        JSONObject data = req.request();
        return data.toString();
    }

    public String getShipName(int id) {
        for (JSONObject ship : ships) {
            if (ship.getInt("api_id")==id) {
                String name = ship.getString("api_name");
                if (ship.getString("api_yomi").equals("elite")) {
                    name += " Elite";
                } else if(ship.getString("api_yomi").equals("flagship")) {
                    name += " Flagship";
                }
                return name;
            }
        }
        return "未知船只: "+id;
    }

    public String getShipType(int id) throws Exception {
        for (JSONObject ship : ships) {
            if (ship.getInt("api_id")==id) {
                return shipTypes[ship.getInt("api_stype")];
            }
        }
        return "?";
    }

    public JSONObject getShipData(int id) throws Exception {
        for (JSONObject ship : ships) {
            if (ship.getInt("api_id")==id) {
                return ship;
            }
        }
        throw new Exception("Ship "+id+" Not Found");
    }

    public String getEquipName(int id) {
        for (JSONObject equip : equipments) {
            if (equip.getInt("api_id")==id) {
                return equip.getString("api_name");
            }
        }
        return "未知设备: "+id;
    }

    public void printDB() throws Exception {
        for (JSONObject ship : ships) {
            System.out.println("#"+ship.getInt("api_id")+" "+getShipName(ship.getInt("api_id")));
        }
    }

    public void fetchShipSwf(int id) throws Exception {
        JSONArray data = getDB().getJSONArray("api_mst_shipgraph");
        for(int i=data.length()-1; i>=0; i--) {
            if (id == data.getJSONObject(i).getInt("api_id")) {
                String resuri = data.getJSONObject(i).getString("api_filename");
//                URL host = new URL(session.url);
                //URL request = new URL("http",host.getHost(),String.format("http://125.6.187.253/kcs/resources/swf/ships/%s.swf",resuri));
                URL request = new URL("http","125.6.187.253",String.format("http://125.6.187.253/kcs/resources/swf/ships/%s.swf",resuri));
                HttpURLConnection con = (HttpURLConnection)request.openConnection();
                File output = new File("./"+getShipName(data.getJSONObject(i).getInt("api_id"))+".swf");

                FileOutputStream out = new FileOutputStream(output);
                InputStream is = con.getInputStream();
                char c;
                while ((c = (char)is.read())!=65535) {
                    out.write(c);
                }
                is.close();
                out.close();
                System.out.println("Written");
            }
        }

    }
}
