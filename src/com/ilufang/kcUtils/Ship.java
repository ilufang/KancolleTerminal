package com.ilufang.kcUtils;

import org.json.JSONObject;


/**
 * Created by ilufang on 4/17/15.
 */
public class Ship {
    public boolean update_flag;
    // Flag used when fleet updates
    // Remove fed/sank/dismantled ships upon update

    private int id, ship, sortno,
                lv, exp, next_exp,
                hp, maxhp,
                fuel, bull,
                dock_time,
                condition,
                firepower, torpedo, armor, antiair,
                dfirepower, dtorpedo, darmor, dantiair,
                maxfirepower, maxtorpedo, maxarmor, maxantiair,
                antisub, spotting, luck, avoidance,
                maxantisub, maxspotting, maxluck, maxavoidance,
                planeslot1, planeslot2, planeslot3, planeslot4,
                equipslot1, equipslot2, equipslot3, equipslot4,
                attacklength, speed;
    private boolean locked;

    public String description;
    private Session session;
    private JSONObject src;

    public Ship(Session session, JSONObject ship) throws Exception{
        this.session = session;
        update(ship);
    }

    public Ship(Session session, int id) throws Exception {
        this.id = id;
        this.session = session;
//        this.description = session.db.getShipName(id);
    }

    public boolean equals(Ship other) {
        return id==other.getId();
    }

    @Override
    public String toString() {
        return description+" (Lv."+lv+")";
    }

    public JSONObject getSrc() {
        return src;
    }

    public void update(JSONObject ship)throws Exception{
        src = ship;
        id = ship.getInt("api_id");
        this.ship = ship.getInt("api_ship_id");
        this.description = session.db.getShipName(this.ship);
        locked = (ship.getInt("api_locked")==1);
        sortno = ship.getInt("api_sortno");
        lv = ship.getInt("api_lv");
        exp = ship.getJSONArray("api_exp").getInt(0);
        next_exp = ship.getJSONArray("api_exp").getInt(1);
        hp = ship.getInt("api_nowhp");
        maxhp = ship.getInt("api_maxhp");
        fuel = ship.getInt("api_fuel");
        bull = ship.getInt("api_bull");
        dock_time = ship.getInt("api_ndock_time");
        condition = ship.getInt("api_cond");
        firepower = ship.getJSONArray("api_karyoku").getInt(0);
        torpedo = ship.getJSONArray("api_raisou").getInt(0);
        armor = ship.getJSONArray("api_soukou").getInt(0);
        antiair = ship.getJSONArray("api_taiku").getInt(0);
        antisub = ship.getJSONArray("api_taisen").getInt(0);
        spotting = ship.getJSONArray("api_sakuteki").getInt(0);
        luck = ship.getJSONArray("api_lucky").getInt(0);
        avoidance = ship.getJSONArray("api_kaihi").getInt(0);
        maxfirepower = ship.getJSONArray("api_karyoku").getInt(1);
        maxtorpedo = ship.getJSONArray("api_raisou").getInt(1);
        maxarmor = ship.getJSONArray("api_soukou").getInt(1);
        maxantiair = ship.getJSONArray("api_taiku").getInt(1);
        maxantisub = ship.getJSONArray("api_taisen").getInt(1);
        maxspotting = ship.getJSONArray("api_sakuteki").getInt(1);
        maxluck = ship.getJSONArray("api_lucky").getInt(1);
        maxavoidance = ship.getJSONArray("api_kaihi").getInt(1);
        dfirepower = ship.getJSONArray("api_kyouka").getInt(0);
        dtorpedo = ship.getJSONArray("api_kyouka").getInt(1);
        darmor = ship.getJSONArray("api_kyouka").getInt(2);
        dantiair = ship.getJSONArray("api_kyouka").getInt(3);
        equipslot1 = ship.getJSONArray("api_slot").getInt(0);
        equipslot2 = ship.getJSONArray("api_slot").getInt(1);
        equipslot3 = ship.getJSONArray("api_slot").getInt(2);
        equipslot4 = ship.getJSONArray("api_slot").getInt(3);
        planeslot1 = ship.getJSONArray("api_onslot").getInt(0);
        planeslot2 = ship.getJSONArray("api_onslot").getInt(1);
        planeslot3 = ship.getJSONArray("api_onslot").getInt(2);
        planeslot4 = ship.getJSONArray("api_onslot").getInt(3);
        attacklength = ship.getInt("api_leng"); // ambiguous
        speed = ship.getInt("api_srate"); // ambiguous
    }


    // auto-generated getters

    public int getId() {
        return id;
    }

    public int getLv() {
        return lv;
    }

    public int getExp() {
        return exp;
    }

    public int getNext_exp() {
        return next_exp;
    }

    public int getHp() {
        return hp;
    }

    public int getMaxhp() {
        return maxhp;
    }

    public int getFuel() {
        return fuel;
    }

    public int getBull() {
        return bull;
    }

    public int getDock_time() {
        return dock_time;
    }

    public int getCondition() {
        return condition;
    }

    public int getFirepower() {
        return firepower;
    }

    public int getTorpedo() {
        return torpedo;
    }

    public int getArmor() {
        return armor;
    }

    public int getAntiair() {
        return antiair;
    }

    public int getDfirepower() {
        return dfirepower;
    }

    public int getDtorpedo() {
        return dtorpedo;
    }

    public int getDarmor() {
        return darmor;
    }

    public int getDantiair() {
        return dantiair;
    }

    public int getAntisub() {
        return antisub;
    }

    public int getSpotting() {
        return spotting;
    }

    public int getLuck() {
        return luck;
    }

    public int getAvoidance() {
        return avoidance;
    }

    public int getPlaneslot1() {
        return planeslot1;
    }

    public int getPlaneslot2() {
        return planeslot2;
    }

    public int getPlaneslot3() {
        return planeslot3;
    }

    public int getPlaneslot4() {
        return planeslot4;
    }

    public int getAttacklength() {
        return attacklength;
    }

    public int getSpeed() {
        return speed;
    }

    public int getShip() {
        return ship;
    }

    public int getSortno() {
        return sortno;
    }

    public int getMaxfirepower() {
        return maxfirepower;
    }

    public int getMaxtorpedo() {
        return maxtorpedo;
    }

    public int getMaxarmor() {
        return maxarmor;
    }

    public int getMaxantiair() {
        return maxantiair;
    }

    public int getMaxantisub() {
        return maxantisub;
    }

    public int getMaxspotting() {
        return maxspotting;
    }

    public int getMaxluck() {
        return maxluck;
    }

    public int getMaxavoidance() {
        return maxavoidance;
    }

    public int getEquipslot1() {
        return equipslot1;
    }

    public int getEquipslot2() {
        return equipslot2;
    }

    public int getEquipslot3() {
        return equipslot3;
    }

    public int getEquipslot4() {
        return equipslot4;
    }

    public boolean isLocked() {
        return locked;
    }
}
