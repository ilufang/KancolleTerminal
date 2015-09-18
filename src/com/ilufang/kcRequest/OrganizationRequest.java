package com.ilufang.kcRequest;

import org.json.JSONObject;

/**
 * Created by ilufang on 4/17/15.
 */
public class OrganizationRequest extends KCRequest {
    public OrganizationRequest(String url, int deck, int slot, int new_ship) throws Exception{
        super(url);
        api_path += "/api_req_hensei/change";
        args += "&api_id="+deck+"&api_ship_idx="+slot+"&api_ship_id="+new_ship;
    }

    public void request() throws Exception{
        String data = connect();
        data = data.substring(7);
        JSONObject response = new JSONObject(data);
        if (response.getInt("api_result")!=1) {
            throw new RequestException("Organize: API Rejected. Code :"+response.get("api_result")+" ["+response.get("api_result_msg")+"]");
        }
    }
}
