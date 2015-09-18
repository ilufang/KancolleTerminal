package com.ilufang.kcInteractive;


import com.ilufang.kcRequest.*;
import com.ilufang.kcUtils.*;
import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by ilufang on 4/29/15.
 */
public class Sparkle {
    private Session session;

    public void prepare() throws Exception {
        CustomRequest req = new CustomRequest(session.url, "api_get_member/mapinfo", "");
        req.request();
        Thread.sleep(1000);
    }


    public char begin(int deck, String map) throws Exception {
        prepare();
        CustomRequest req = new CustomRequest(session.url, "api_req_map/start", "&api_formation=1&api_maparea_id=1&api_mapinfo_no="+map+"&api_deck_id="+deck);
        JSONObject data = req.request();
        if (data.getJSONObject("api_data").getJSONObject("api_enemy").getInt("api_result")==1) {
            return (char)(data.getJSONObject("api_data").getInt("api_no")-1+'A');
        } else {
            throw new RequestException("# [ERROR] Could not parse response from api_req_map/start, returned json:"+data);
        }
    }

    public JSONObject battle() throws Exception {
        CustomRequest req = new CustomRequest(session.url, "api_req_sortie/battle", "&api_formation_id=1&api_recovery_type=0");
        JSONObject data = req.request();
        Battle.battleDelay(data.getJSONObject("api_data"));
        return data;
    }

    public JSONObject battleResult() throws Exception {
        CustomRequest req = new CustomRequest(session.url, "api_req_sortie/battleresult", "");
        JSONObject data = req.request().getJSONObject("api_data");
        session.fleet.update();
        return data;
    }


    public JSONArray resupply(int deck) throws Exception {
        String ships = "";
        Deck objDeck = new Deck(session, deck);
        for (Ship ship : objDeck.getShips()) {
            ships += ',';
            ships += ship.getId();
        }
        ResupplyRequest req = new ResupplyRequest(session.url, ResupplyRequest.SUPPLYALL, ships.substring(1));
        return req.request().getJSONArray("api_material");
    }

    public Sparkle(Session session) {
        this.session = session;
    }

    public void sparkle(int deck, int slot, String maparea, boolean support) throws Exception {
        slot -= 1;
        session.fleet.update();

        // Save original Ship
        Deck activeDeck = new Deck(session, deck);
        List<Ship> activeShips = activeDeck.getShips();
        Ship objShip = activeDeck.getShip(slot);
        int[] originalID = new int[6];
        for (int i=0; i<6; i++) {
            originalID[i]=-1;
        }

        for (int i=0; i<activeShips.size(); i++) {
            originalID[i] = activeShips.get(i).getId();
        }

        // Switch to ship
        System.out.println("Sparkling "+objShip);

        new OrganizationRequest(session.url, deck, 0, objShip.getId()).request();
        for (int i=5; i>0; i--) {
            new OrganizationRequest(session.url, deck, i, -1).request();
        }

        if (support) {
            System.out.println("Adding supporting ships...");
            // Add aux ships
            //String activeFleet[] = new String[6];
            List<String> activeFleet = new ArrayList<String>();
            activeFleet.add(objShip.description);
            for (Ship ship : session.fleet.getShips()) {
                if (activeFleet.size() <= 5 && !ship.isLocked() && session.db.getShipType(ship.getShip()).equals("DD")) {
                    boolean accept = true;
                    for (String shipName : activeFleet) {
                        if (shipName.equals(ship.description)) {
                            accept = false;
                        }
                    }
                    if (accept) {
                        activeFleet.add(ship.description);
                        System.out.print(ship.description + " ");
                        OrganizationRequest req = new OrganizationRequest(session.url, deck, activeFleet.size()-1, ship.getId());
                        req.request();
                    }
                }
            }
            System.out.println();
        }

        // Battle 1-1
        System.out.println("Battling...");

        for (int count=0; count<3; count++) {
            begin(deck, maparea);
            Thread.sleep(1000);
            battle();
            Thread.sleep(5000);
            JSONObject data = battleResult();
            System.out.print(data.getString("api_win_rank")+data.getInt("api_mvp")+" ");
            if (objShip.getHp()<=objShip.getMaxhp()*0.50) {
                System.out.println();
                System.out.println("Ship damaged.");
                break;
            }
        }
        System.out.println();
        System.out.println("Please refresh homeport");
        System.out.print("Press ENTER to continue...");
        try {
            System.in.read();
        } catch (Exception e) {}

        System.out.println("Resupply...");
        resupply(deck);

        // Restore
        System.out.println("Restoring Organization...");
        for (int i=0; i<6; i++) {
            new OrganizationRequest(session.url, deck, i, originalID[i]).request();
        }
    }
}
