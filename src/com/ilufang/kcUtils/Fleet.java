package com.ilufang.kcUtils;

import com.ilufang.kcRequest.GetFleetRequest;
import org.json.*;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by ilufang on 4/17/15.
 */
public class Fleet {
    private List<Ship> ships;
    private String url;
    private Session session;
    public Fleet(Session session) throws Exception {
        this.session = session;
        this.url = session.url;
        ships = new GetFleetRequest(session).request();
    }

    public void update() throws Exception {
        /*
        List<Ship> newships = new GetFleetRequest(session).request();
        for (Ship ship : ships) {
            ship.update_flag = false;
        }
        for (Ship ship : newships) {
            getShip(ship.getId()).update(ship.getSrc());
            getShip(ship.getId()).update_flag = true;
        }
        for (int i=ships.size()-1; i>=0; i--) {
            if (!ships.get(i).update_flag) {
                ships.remove(i);
            }
        }
        */
    }

    public void update(JSONArray fleet) throws Exception {
        List<Ship> newships = new ArrayList<Ship>();
        for (int i=fleet.length()-1; i>=0; i--) {
            newships.add(new Ship(session, fleet.getJSONObject(i)));
        }
        for (Ship ship : ships) {
            ship.update_flag = false;
        }
        for (Ship ship : newships) {
            getShip(ship.getId()).update(ship.getSrc());
            getShip(ship.getId()).update_flag = true;
        }
        for (int i=ships.size()-1; i>=0; i--) {
            if (!ships.get(i).update_flag) {
                ships.remove(i);
            }
        }
    }

    public String getUrl() {
        return url;
    }

    public Ship getShip(int id) throws Exception {
        for (Ship ship : ships) {
            if (ship.getId()==id) {
                return ship;
            }
        }
        Ship newship = new Ship(session,id);
        ships.add(newship);
        return newship;
    }

    public List<Ship> getShips() {
        return ships;
    }

}
