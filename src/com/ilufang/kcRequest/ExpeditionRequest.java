package com.ilufang.kcRequest;

/**
 * Created by ilufang on 4/17/15.
 */
public class ExpeditionRequest extends KCRequest {
    public ExpeditionRequest(String url, int deck, int mission, int missionapi) throws Exception {
        super(url);
        api_path += "/api_req_mission/start";
        args += "api_deck_id="+deck+"&api_mission_id="+mission+"&api_mission="+missionapi;
    }
    public void request() throws Exception{
        System.out.println(connect());
    }
}
