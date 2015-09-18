package com.ilufang.kcRequest;

import org.json.JSONObject;

/**
 * Created by ilufang on 4/18/15.
 */
public class CustomRequest extends KCRequest {
    public CustomRequest(String url, String path, String args) throws Exception{
        super(url);
        api_path += "/"+path;
        this.args += args;
    }

    public JSONObject request() throws Exception{
        String data = connect();
        JSONObject response = new JSONObject(data.substring(7));
        if (response.getInt("api_result")!=1) {
            throw new RequestException("API Request: API Rejected. Code :"+response.get("api_result")+" ["+response.get("api_result_msg")+"]");
        }
        return response;
    }
}
