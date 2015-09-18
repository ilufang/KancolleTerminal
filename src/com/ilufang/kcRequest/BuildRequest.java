package com.ilufang.kcRequest;

import org.json.JSONObject;

/**
 * Created by ilufang on 4/21/15.
 */
public class BuildRequest extends KCRequest {
    private String url;
    private int slot;

    public BuildRequest(String url, int slot, int fuel, int bull, int stel, int baux, int seaweed, boolean lsc, boolean fire) throws Exception{
        super(url);
        this.url = url;
        this.slot = slot;
        api_path += "/api_req_kousyou/createship";
        args+="&api_item1="+fuel+"&api_item2="+bull+"&api_item3="+stel+"&api_item4="+baux+"&api_item5="+seaweed+"&api_large_flag="+(lsc?1:0)+"&api_kdock_id="+slot+"&api_highspeed="+(fire?1:0);
    }

    public void request() throws Exception{
        JSONObject construct = new JSONObject(connect().substring(7));
        if (construct.getInt("api_result")!=1) {
            throw new RequestException("Construct: API Rejected. Code:"+construct.getInt("api_result")+" Message:"+construct.getString("api_result_msg"));
        }
    }
}
