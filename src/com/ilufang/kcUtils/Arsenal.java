package com.ilufang.kcUtils;

import com.ilufang.kcRequest.BuildRequest;
import com.ilufang.kcRequest.CustomRequest;
import com.ilufang.kcRequest.DevelopRequest;
import org.json.JSONObject;

/**
 * Created by ilufang on 4/21/15.
 */
public class Arsenal {
    private Session session;
    public Arsenal(Session session) {
        this.session = session;
    }

    public String construct(int slot, int fuel, int bull, int stel, int baux, boolean fire) throws Exception {
        BuildRequest req = new BuildRequest(session.url, slot, fuel, bull, stel, baux, 1, false, fire);
        req.request();
        return get(slot);
    }

    public String constructLarge(int slot, int fuel, int bull, int stel, int baux, int seaweed) throws Exception {
        BuildRequest req = new BuildRequest(session.url, slot, fuel, bull, stel, baux, seaweed, true, false);
        req.request();
        return get(slot);
    }

    public String constructLargeInsta(int slot, int fuel, int bull, int stel, int baux, int seaweed) throws Exception {
        BuildRequest req = new BuildRequest(session.url, slot, fuel, bull, stel, baux, seaweed, true, true);
        req.request();
        String result = get(slot);
        CustomRequest claim = new CustomRequest(session.url, "api_req_kousyou/getship", "&api_kdock+id="+slot);
        claim.request();
        return result;
    }

    public void build(int slot, int fuel, int bull, int stel, int baux) throws Exception {
        System.out.print("Build: "+fuel+"/"+bull+"/"+stel+"/"+baux+" @"+slot+" : ");
        System.out.println(construct(slot, fuel, bull, stel, baux, false));
    }

    public void build(int slot, int fuel, int bull, int stel, int baux, double rate) throws Exception {
        double f = fuel*(Math.random()*rate*2+1-rate);
        double b = bull*(Math.random()*rate*2+1-rate);
        double s = stel*(Math.random()*rate*2+1-rate);
        double a = baux*(Math.random()*rate*2+1-rate);
        if (f<30) f=30;
        if (b<30) b=30;
        if (s<30) s=30;
        if (a<30) a=30;
        if (f>999) f=999;
        if (b>999) b=999;
        if (s>999) s=999;
        if (a>999) a=999;
        build(slot, (int)f, (int)b, (int)s, (int)a);
    }

    public String buildinstant(int slot, int fuel, int bull, int stel, int baux, double rate) throws Exception{
        double f = fuel*(Math.random()*rate*2+1-rate);
        double b = bull*(Math.random()*rate*2+1-rate);
        double s = stel*(Math.random()*rate*2+1-rate);
        double a = baux*(Math.random()*rate*2+1-rate);
        if (f<30) f=30;
        if (b<30) b=30;
        if (s<30) s=30;
        if (a<30) a=30;
        if (f>999) f=999;
        if (b>999) b=999;
        if (s>999) s=999;
        if (a>999) a=999;
        System.out.print("InstaBuild: " + (int) f + "/" + (int) b + "/" + (int) s + "/" + (int) a + " @" + slot + " :");
        String result = construct(slot, (int)f, (int)b, (int)s, (int)a, true);
        System.out.println(result);
        CustomRequest claim = new CustomRequest(session.url, "api_req_kousyou/getship", "&api_kdock+id="+slot);
        claim.request();
        return result;
    }

    public void buildbatch(int slot, int count, int fuel, int ammo, int steel, int baux, double rate) throws Exception {
        for (int i=0; i<count; i++) {
            buildinstant(slot, fuel, ammo, steel, baux, rate);
        }
    }

    public String develop(int fuel, int ammo, int steel, int baux) throws Exception {
        DevelopRequest req = new DevelopRequest(session.url, fuel, ammo, steel, baux);
        System.out.print("Develop: "+fuel+"/"+ammo+"/"+steel+"/"+baux+" : ");
        int result = req.request();
        if (result == -1) {
            System.out.println("Fail");
            return "开发失败";
        } else {
            System.out.println(session.db.getEquipName(result));
            return session.db.getEquipName(result);
        }
    }

    public void developbatch(int count, int fuel, int ammo, int steel, int baux) throws Exception {
        for (int i=0; i<count; i++) {
            develop(fuel, ammo, steel, baux);
        }
    }

    public String get(int slot) throws Exception{
        CustomRequest retrieve = new CustomRequest(session.url, "api_get_member/kdock", "");
        JSONObject docks = retrieve.request();
        JSONObject dock = docks.getJSONArray("api_data").getJSONObject(slot-1);
        int ship = dock.getInt("api_created_ship_id");
        return session.db.getShipName(ship);
    }
}
