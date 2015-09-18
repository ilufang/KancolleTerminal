package com.ilufang.kcUtils;

import com.ilufang.kcRequest.GetDeckRequest;
import org.json.*;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by ilufang on 4/18/15.
 */
public class Deck {
    private List<Ship> ships;
    private String name;
    private int deckno;
    private GetDeckRequest req;
    private Fleet fleet;
    public Deck (Session master, int deck) throws Exception {
        this.fleet = master.fleet;
        this.ships = new ArrayList<>();
        this.deckno = deck;
        req = new GetDeckRequest(master.url);
        JSONObject data = req.getDeck(deck);
        name = data.getString("api_name");
        int ship, i=0;
        while (i<6 && (ship = data.getJSONArray("api_ship").getInt(i))!=-1) {
            ships.add(fleet.getShip(ship));
            i++;
        }
    }
    public Ship getShip(int index) {
        return ships.get(index);
    }

    public List<Ship> getShips() {
        return ships;
    }

    public void setDeck(int deck) throws Exception{
        deckno = deck;
        JSONObject data = req.getDeck(deck);
        name = data.getString("api_name");
        int ship, i=0;
        ships.clear();
        while (i<6 && (ship = data.getJSONArray("api_ship").getInt(i))!=-1) {
            ships.add(fleet.getShip(ship));
            i++;
        }
    }

    public void update() throws Exception {
        req.invalidate();
        setDeck(deckno);
    }

    public int getIndex() {
        return deckno;
    }

    @Override
    public String toString() {
        return ""+deckno+"/ "+name;
    }
}
