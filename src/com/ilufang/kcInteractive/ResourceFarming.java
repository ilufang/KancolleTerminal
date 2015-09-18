package com.ilufang.kcInteractive;

import com.ilufang.kcRequest.CustomRequest;
import com.ilufang.kcUtils.Session;
import org.json.JSONObject;

/**
 * Created by ilufang on 5/10/15.
 */
public class ResourceFarming {
    private Session session;

    public ResourceFarming(Session session) {
        this.session = session;
    }

    public int[] farm(int map_major, int map_minor, int deck) throws Exception {
        sortie_max = 0;
        switch (map_major*10+map_minor) {
            case 12:
            case 23:
            case 22:
                sortie_max=1;
            case 14:
                sortie_max=2;
                break;
            default:
                throw new Exception("无效的地图.");
        }
        int[] result = beginmap(map_major,map_minor,deck);
        Thread.sleep(5000*(int)(Math.pow(Math.random(),2)));
        return result;
    }

    public int[] farm(int[] maps_major, int[] maps_minor, int deck) throws Exception {
        int[] result = {0,0,0,0};
        for (int i=0; i<maps_major.length; i++) {
            int[] ret = farm(maps_major[i],maps_minor[i],deck);
            for (int j=0; j<ret.length; j++) {
                result[i]+=ret[i];
            }
        }
        return result;
    }

    int next, sortie_count, sortie_max;

    private int[] beginmap(int major_map, int minor_map, int deck) throws Exception {
        sortie_count = 0;
        CustomRequest req = new CustomRequest(session.url, "api_req_map/start", "&api_formation=1&api_maparea_id="+major_map+"&api_mapinfo_no="+minor_map+"&api_deck_id="+deck);
        JSONObject data = req.request().getJSONObject("api_data");
        return encounterNode(data);
    }

    private int[] encounterNode(JSONObject data) throws Exception {
        Thread.sleep(1000);
        sortie_count++;
        next = data.getInt("api_next");
        int[] result = {0,0,0,0};
        switch (data.getInt("api_event_id")) {
            case 4:
            case 5:
                // Battle Node, ignore
                //battle();
                return result;
            case 2:
                // Resource Node
                int res_type = data.getJSONObject("api_itemget").getInt("api_id")-1; // Index begins from 1
                int res_count = data.getJSONObject("api_itemget").getInt("api_getcount");
                result[res_type]=res_count;
                int[] upcoming_resources = advance();
                for (int i=0; i<upcoming_resources.length; i++) {
                    result[i]+=upcoming_resources[i];
                }
                Thread.sleep(2000);
                return result;
            case 3:
                // Storm, considered an error
                throw new Exception("遭遇风暴节点");
            default:
                throw new Exception("未知节点类型"+data.getInt("api_event_id"));
        }
    }

    private int[] advance() throws Exception {
        if (next!=0) {
            CustomRequest req = new CustomRequest(session.url, "api_req_map/next", "&api_recovery_type=0");
            JSONObject data = req.request().getJSONObject("api_data");
            return encounterNode(data);
        } else {
            int[] result={0,0,0,0};
            return result;
        }
    }


}
