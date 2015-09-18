package com.ilufang.kcRequest;

import org.json.*;

/**
 * Created by ilufang on 4/18/15.
 */
public class GetDeckRequest extends KCRequest{
    public GetDeckRequest(String url) throws Exception{
        super(url);
        api_path += "/api_get_member/deck";
    }

    private JSONArray saved_deck;

    private void request() throws Exception {
        String data = connect();
        data = data.substring(7);
        JSONObject response = new JSONObject(data);
        if (response.getInt("api_result")!=1) {
            throw new RequestException("Get Deck: API Rejected. Code :"+response.get("api_result")+" ["+response.get("api_result_msg")+"]");
        }
        saved_deck = response.getJSONArray("api_data");
    }

    public JSONObject getDeck(int deck) throws Exception {
        if (saved_deck==null) {
            request();
        }
        return saved_deck.getJSONObject(deck-1);
    }

    public void invalidate() {
        saved_deck = null;
    }
}
