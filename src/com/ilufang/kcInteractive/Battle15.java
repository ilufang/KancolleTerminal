package com.ilufang.kcInteractive;

import com.ilufang.kcRequest.CustomRequest;
import com.ilufang.kcRequest.OrganizationRequest;
import com.ilufang.kcRequest.RequestException;
import com.ilufang.kcRequest.ResupplyRequest;
import com.ilufang.kcUtils.Deck;
import com.ilufang.kcUtils.Fleet;
import com.ilufang.kcUtils.Session;
import com.ilufang.kcUtils.Ship;
import org.json.JSONArray;
import org.json.JSONObject;

import java.util.Scanner;

/**
 * Created by ilufang on 4/19/15.
 */
public class Battle15 {
    private Deck deck;
    private Session session;
    private int active_flagship;

    public Battle15(Session session, int deck) throws Exception {
        active_flagship = 0;
        this.session = session;
        this.deck = new Deck(session,deck);

    }

    public Battle15(Session session) throws Exception {
        active_flagship = 0;
        this.session = session;
        interactive();
    }


    private void interactive() throws Exception {
        Scanner in = new Scanner(System.in);
        System.out.print("Enter Fleet No. (1-4):");
        int deckno = in.nextInt();
        deck = new Deck(session, deckno);
        System.out.println();
        System.out.println(deck);
        int i=0;
        for (Ship ship: deck.getShips()) {
            i++;
            System.out.println(""+i+"/"+ship+" HP:"+ship.getHp()+"/"+ship.getMaxhp()+" Cond:"+ship.getCondition());
        }
        System.out.println();
        System.out.print("Is it ok? Type Y to continue...");
        if (!in.next().equals("Y")) {
            System.out.println("Sortie cancelled");
            return;
        }
        int count = 0;

        while (true) {
            for (int bat_cnt = 0; bat_cnt < 3; bat_cnt++) {
                count++;

                if (deck.getShip(0).getCondition()<25) {
                    System.out.println("Ship Condition Too Low.");
                    return;
                }
                if (deck.getShip(0).getMaxhp()!=deck.getShip(0).getHp()) {
                    System.out.println("Ship Damaged. Please Fix.");
                    return;
                }

                System.out.println("[#" + count + "] Begin Node " + begin());
                Thread.sleep(2000);

                System.out.println("Battling...");
                battle();
                Thread.sleep(5000);

                JSONObject data = battleResult();
                System.out.println();
                System.out.println("Battle Result: " + data.getString("api_quest_name") + " " + data.getJSONObject("api_enemy_info").getString("api_deck_name"));
                System.out.println("================");
                System.out.print("RANK: " + data.getString("api_win_rank"));
                System.out.println(" MVP: " + data.getInt("api_mvp") + " BaseXP: " + data.getInt("api_get_base_exp"));
                System.out.println();
                i = 0;
                for (Ship ship : deck.getShips()) {
                    i++;
                    System.out.print("" + deck.getShip(i - 1) + " EXP+" + data.getJSONArray("api_get_ship_exp").getInt(i)); // Data returned indexes from 1 to 6
                    System.out.print(" (" + data.getJSONArray("api_get_exp_lvup").getJSONArray(i - 1).getInt(0) + "/" + data.getJSONArray("api_get_exp_lvup").getJSONArray(i - 1).getInt(1) + ")");
                    System.out.print(" Lv." + ship.getLv());
                    System.out.print(" HP: " + ship.getHp() + "/" + ship.getMaxhp());
                    System.out.println();
                }
                System.out.println();
                if (data.getJSONArray("api_get_flag").getInt(1) != 0) {
                    // Get new ship
                    System.out.println("+ Discovered: " + data.getJSONObject("api_get_ship").getString("api_ship_name"));
                    System.out.println(data.getJSONObject("api_get_ship").getString("api_ship_getmes"));
                    System.out.println();
                }

            }


            System.out.println("Now Switch and return to homeport in your swf!");
            System.out.print("Press ENTER to continue (q to abort)...");
            try {
                if (System.in.read()=='q') {
                    return;
                }
            } catch (Exception e) {}


            JSONArray res = resupply();
            System.out.println("Resupplied Fleet. Remaining Resources:");
            System.out.println("FUEL: " + res.getInt(0) + " AMMO:" + res.getInt(1) + " STEEL:" + res.getInt(2) + " BAUX:" + res.getInt(3));
            Thread.sleep(2000);
        }

    }

    private void prepare() throws Exception {
        CustomRequest req = new CustomRequest(session.url, "api_get_member/mapinfo", "");
        req.request();
        Thread.sleep(1000);
    }


    public char begin() throws Exception {
        return begin(1,5);
    }

    public char begin(int map_major, int map_minor) throws Exception {
        prepare();
        CustomRequest req = new CustomRequest(session.url, "api_req_map/start", "&api_formation=1&api_maparea_id="+map_major+"&api_mapinfo_no="+map_minor+"&api_deck_id="+deck.getIndex());
        JSONObject data = req.request();
        if (data.getJSONObject("api_data").getJSONObject("api_enemy").getInt("api_result")==1) {
            return (char)(data.getJSONObject("api_data").getInt("api_no")-1+'A');
        } else {
            throw new RequestException("# [ERROR] Could not parse response from api_req_map/start, returned json:"+data);
        }
    }

    public JSONObject battle() throws Exception {
        CustomRequest req = new CustomRequest(session.url, "api_req_sortie/battle", "&api_formation_id=5&api_recovery_type=0");
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

    public char advance() throws Exception {
        CustomRequest req = new CustomRequest(session.url, "api_req_map/next", "&api_recovery_type=0");
        JSONObject data = req.request();
        if (data.getJSONObject("api_data").getJSONObject("api_enemy").getInt("api_result")==1) {
            return (char)(data.getJSONObject("api_data").getInt("api_no")-1+'A');
        } else {
            throw new RequestException("# [ERROR] Could not parse response from api_req_map/next, returned json:"+data);
        }
    }

    public JSONArray resupply() throws Exception {
        String ships = "";
        for (Ship ship : deck.getShips()) {
            ships += ',';
            ships += ship.getId();
        }
        ResupplyRequest req = new ResupplyRequest(session.url, ResupplyRequest.SUPPLYALL, ships.substring(1));
        return req.request().getJSONArray("api_material");
    }

    public int next_flagship() throws Exception {
        OrganizationRequest req;
        // Redo request to restore original organization
        req = new OrganizationRequest(session.url, deck.getIndex(), 0, deck.getShip(active_flagship).getId());
        req.request();
        deck.update();

        // Update flagship index
        active_flagship++;
        if (active_flagship >= deck.getShips().size()) {
            active_flagship = 0;
        }

        // Put ship at new index as the flagship
        req = new OrganizationRequest(session.url, deck.getIndex(), 0, deck.getShip(active_flagship).getId());
        req.request();
        deck.update();
        return active_flagship;
    }


    public Deck getDeck() {
        return deck;
    }

    public int getActive_flagship() {
        return active_flagship;
    }

    public Session getSession() {
        return session;
    }

}
