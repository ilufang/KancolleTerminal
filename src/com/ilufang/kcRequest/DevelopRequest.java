package com.ilufang.kcRequest;

import org.json.JSONObject;

/**
 * Created by ilufang on 4/21/15.
 */
public class DevelopRequest extends KCRequest {
    public DevelopRequest(String url, int fuel, int ammo, int steel, int baux) throws Exception {
        super(url);
        api_path += "/api_req_kousyou/createitem";
        args+="&api_item1="+fuel+"&api_item2="+ammo+"&api_item3="+steel+"&api_item4="+baux;
    }

    public int request() throws Exception {
        JSONObject data = new JSONObject(connect().substring(7));
        if (data.getInt("api_result")!=1) {
            throw new RequestException("Develop: API Rejected. Code:"+data.getInt("api_result")+" Message:"+data.getString("api_result_msg"));
        }
        if (data.getJSONObject("api_data").getInt("api_create_flag")!=1) {
            // failure
            return -1;
        } else {
            return data.getJSONObject("api_data").getJSONObject("api_slot_item").getInt("api_slotitem_id");
        }
    }
}
