package com.ilufang.kcInteractive;

import com.ilufang.kcLogging.DropLogging;
import com.ilufang.kcLogging.KCLogger;
import com.ilufang.kcRequest.CustomRequest;
import com.ilufang.kcRequest.ResupplyRequest;
import com.ilufang.kcUtils.Deck;
import com.ilufang.kcUtils.Session;
import com.ilufang.kcUtils.Ship;
import org.json.JSONObject;

import java.util.Date;

/**
 * Created by root on 5/13/15.
 */
public class E2Unryuu {
    Session session;

    public E2Unryuu(Session session) {
        this.session = session;
    }

    public String battle() throws Exception {
        Thread.sleep(1000);
        session.fleet.update();
        Deck deck = new Deck(session, 1);
        for (Ship ship : deck.getShips()) {
            if (ship.getCondition()<30) {
                return "Condition Too Low: "+ship.toString();
            }
            if (ship.getHp()<=ship.getMaxhp()/4) {
                return "Heavily Damaged: "+ship.toString();
            }
        }
        deck.setDeck(2);
        for (Ship ship : deck.getShips()) {
            if (ship.getCondition()<30) {
                return "Condition Too Low: "+ship.toString();
            }
            if (ship.getHp()<=ship.getMaxhp()/4) {
                return "Heavily Damaged: "+ship.toString();
            }
        }
        Thread.sleep(1000);
        new CustomRequest(session.url, "api_get_member/mapinfo", "").request();
        new CustomRequest(session.url, "api_get_member/sortie_conditions", "").request();
        Thread.sleep(1000);
        new CustomRequest(session.url, "api_get_member/mapcell", "&api_maparea_id=30&api_mapinfo_no=2").request();
        Thread.sleep(1000);
        new CustomRequest(session.url, "api_req_map/start", "&api_formation_id=1&api_maparea_id=30&api_mapinfo_no=2&api_deck_id=1").request();
        Thread.sleep(1000);
        new CustomRequest(session.url, "api_req_map/next", "&api_recovery_type=0").request();
        Thread.sleep(1000);
        new CustomRequest(session.url, "api_req_map/next", "&api_recovery_type=0&api_cell_id=8").request();
        Thread.sleep(1000);
        new CustomRequest(session.url, "api_req_map/next", "&api_recovery_type=0").request();
        Thread.sleep(1000);
        JSONObject battle_status = new CustomRequest(session.url, "api_req_combined_battle/battle", "&api_recovery_type=0&api_formation=14").request().getJSONObject("api_data");
        Thread.sleep(5500);
        if (battle_status.getInt("api_midnight_flag")!=0) {
            // Night battle
            new CustomRequest(session.url, "api_req_combined_battle/midnight_battle", "&api_recovery_type=0").request();
            Thread.sleep(5500);
        }

        JSONObject result = new CustomRequest(session.url, "api_req_combined_battle/battleresult", "").request().getJSONObject("api_data");
        session.fleet.update();
        if (result.getJSONArray("api_get_flag").getInt(1)!=0) {
            // Get new ship
            for (KCLogger logger : session.loggers) {
                if (logger.getClass()== DropLogging.class) {
                    ((DropLogging)logger).addEntry(30,2,10,result.getJSONObject("api_get_ship").getString("api_ship_name"),result.getString("api_win_rank"));
                }
            }
            destroy_latest = true;
            if (result.getJSONObject("api_get_ship").getString("api_ship_name").equals("雲龍")) {
                destroy_latest = false;
            }
            return result.getJSONObject("api_get_ship").getString("api_ship_type")+" "+result.getJSONObject("api_get_ship").getString("api_ship_name");
        }
        for (KCLogger logger : session.loggers) {
            if (logger.getClass().equals(DropLogging.class)) {
                ((DropLogging)logger).addEntry(30,2,10,"-",result.getString("api_win_rank"));
            }
        }
        destroy_latest = false;
        return "No Loot.";
    }

    private void resupply() throws Exception {
        Deck deck = new Deck(session, 1);
        String ships = "";
        for (Ship ship : deck.getShips()) {
            ships += ',';
            ships += ship.getId();
        }
        ResupplyRequest req = new ResupplyRequest(session.url, ResupplyRequest.SUPPLYALL, ships.substring(1));
        req.request();
        deck.setDeck(2);
        ships = "";
        for (Ship ship : deck.getShips()) {
            ships += ',';
            ships += ship.getId();
        }
        req = new ResupplyRequest(session.url, ResupplyRequest.SUPPLYALL, ships.substring(1));
        req.request();
    }

    private int repair() throws Exception {
        Deck deck = new Deck(session,1);
        for (Ship ship : deck.getShips()) {
            if (ship.getHp()<0.7*ship.getMaxhp()) {
                // lightly damaged
                // fix immediately
                CustomRequest repair = new CustomRequest(session.url, "api_req_nyukyo/start", "&api_ship_id="+ship.getId()+"&api_ndock_id=1&api_highspeed=1");
                repair.request();
            }
        }
        deck.setDeck(2);
        int repair_time = 0;
        for (Ship ship : deck.getShips()) {
            if (session.db.getShipType(ship.getShip()) != "SS" && ship.getHp() < 0.75 * ship.getMaxhp()) {
                // lightly damaged
                // fix immediately
                CustomRequest repair = new CustomRequest(session.url, "api_req_nyukyo/start", "&api_ship_id=" + ship.getId() + "&api_ndock_id=1&api_highspeed=1");
                repair.request();
            } else if (session.db.getShipType(ship.getShip()) == "SS" && ship.getHp() < 0.3 * ship.getMaxhp()) {
                CustomRequest repair = new CustomRequest(session.url, "api_req_nyukyo/start", "&api_ship_id=" + ship.getId() + "&api_ndock_id=2&api_highspeed=0");
                repair.request();
                repair = new CustomRequest(session.url, "api_get_member/ndock", "");
                JSONObject data = repair.request().getJSONArray("api_data").getJSONObject(1);
                long finish_t = data.getInt("api_complete_time");
                Date date = new Date();
                repair_time = (int) (finish_t - date.getTime()); // ms
            }
        }
        return repair_time;
    }

    private boolean destroy_latest;

    public int fixes() throws Exception{
        session.fleet.update();
        destroyUnwanted();
        resupply();
        int time = repair();
        session.fleet.update();
        // Conditions
        Deck deck = new Deck(session, 1);
        for (Ship ship : deck.getShips()) {
            if (ship.getCondition()<40) {
                int regen_time = ((45-ship.getCondition())/3+1)*3*60*1000;
                if (time < regen_time) {
                    time = regen_time;
                }
            }
        }
        deck.setDeck(2);
        for (Ship ship : deck.getShips()) {
            if (ship.getCondition()<40) {
                int regen_time = ((45-ship.getCondition())/3+1)*3*60*1000;
                if (time < regen_time) {
                    time = regen_time;
                }
            }
        }


        return time;
    }

    private boolean destroyUnwanted() throws Exception {
        if (!destroy_latest) {
            return false;
        }
        int last=0;
        for (Ship ship : session.fleet.getShips()) {
            if (ship.getId()>last) {
                last = ship.getId();
            }
        }
        int lastship = session.fleet.getShip(last).getShip();

        if (!session.db.getShipType(lastship).equals("CV")) {
            // Destroy that.
            Thread.sleep(1000);
            CustomRequest req = new CustomRequest(session.url, "api_req_kousyou/destroyship", "&api_ship_id="+last);
            req.request();
            Thread.sleep(1000);
            return true;
        }
        return false;
    }
}
