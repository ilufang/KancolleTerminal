package com.ilufang.kcRequest;

import org.json.JSONObject;

/**
 * Created by ilufang on 4/17/15.
 */
public class ResupplyRequest extends KCRequest {
    public static final int    FUEL_ONLY = 1,
                        AMMO_ONLY = 2,
                        SUPPLYALL = 3;

    public ResupplyRequest(String url, int kind, int ship) throws Exception {
        super(url);
        args += "&api_kind="+kind+"&api_onslot=1&api_id_items="+ship;
        api_path += "/api_req_hokyu/charge";
    }

    public ResupplyRequest(String url, int kind, String ships) throws Exception {
        super(url);
        args += "&api_kind="+kind+"&api_onslot=1&api_id_items="+ships;
        api_path += "/api_req_hokyu/charge";
    }

    public JSONObject request() throws Exception {
        JSONObject response = new JSONObject(connect().substring(7));
        if (response.getInt("api_result")!=1) {
            throw new RequestException("Resupply: API Rejected. Path:"+api_path+" Args:"+args+" Code :"+response.get("api_result")+" ["+response.get("api_result_msg")+"]");
        }
        return response.getJSONObject("api_data");
    }

}
