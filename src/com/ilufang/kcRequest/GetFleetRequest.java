package com.ilufang.kcRequest;

import com.ilufang.kcUtils.*;
import org.json.*;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by ilufang on 4/17/15.
 */
public class GetFleetRequest extends KCRequest {
    private Session session;

    public GetFleetRequest(Session session) throws Exception{
        super(session.url);
        this.session = session;
        api_path += "/api_get_member/ship2";
        args += "&api_sort_key=1&api_sort_order=1";
    }
    public List<Ship> request() throws Exception{
        String data = connect();
        data = data.substring(7);
        JSONObject response = new JSONObject(data);
        if (response.getInt("api_result")!=1) {
            throw new RequestException("Get Full Fleet: API Rejected. Code :"+response.get("api_result")+" ["+response.get("api_result_msg")+"]");
        }

        JSONArray fleet = response.getJSONArray("api_data");

        List<Ship> ships = new ArrayList<Ship>();

        for (int i=fleet.length()-1; i>=0; i--) {
            ships.add(new Ship(session, fleet.getJSONObject(i)));
        }

        return ships;

    }
}
