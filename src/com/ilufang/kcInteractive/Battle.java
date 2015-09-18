package com.ilufang.kcInteractive;

import com.ilufang.kcRequest.CustomRequest;
import com.ilufang.kcRequest.RequestException;
import com.ilufang.kcUtils.*;
import org.json.*;

import javax.swing.plaf.SliderUI;
import java.util.Scanner;

/**
 * Created by ilufang on 4/17/15.
 */
public class Battle {
    private String major_map, minor_map;
    private int deck;
    private Fleet fleet;
    private Deck battledeck;
    Session session;

    // Node record
    private int next;

    public Battle(Session session, int major_map, int minor_map, int fleet) throws Exception {
        this.session = session;
        this.major_map = String.format("%d",major_map);
        this.minor_map = String.format("%d",minor_map);
        this.deck = fleet;
        this.fleet = new Fleet(session);
        sortie();
    }
    public Battle(Session session) throws Exception {
        this.session = session;
        this.fleet = session.fleet;
        selectMap();
    }

    private void selectMap() throws Exception {
        CustomRequest req = new CustomRequest(session.url, "api_get_member/mapinfo", "");
        JSONObject data = req.request();
        JSONArray maps = data.getJSONArray("api_data");
        System.out.println("选择地图:");
        System.out.println();

        System.out.println("警告: 请务必按要求选择");
        System.out.println("程序不会检查选择的合法性,非法选择的请求将仍然被发送");
        System.out.println("选择未开启或不存在的地图可能导致临时的或永久的封号");
        System.out.println();

        for (int i = 0; i < maps.length(); i++) {
            JSONObject map = maps.getJSONObject(i);
            if (map.getInt("api_cleared")==0) {
                System.out.print("[NEW]     ");
            } else {
                System.out.print("[CLEARED] ");
            }
            int id = map.getInt("api_id");
            System.out.println(" " + id / 10 + "-" + id % 10);
        }
        Scanner in = new Scanner(System.in);
        System.out.println();
        System.out.print("选择地图 (如1-2):");
        String levels = in.next();
        major_map = levels.substring(0,1);
        minor_map = levels.substring(2, 3);
        selectFleet();
    }

    private void selectFleet() throws Exception {
        System.out.println();
        System.out.println("选择舰队:");
        System.out.println();

        System.out.println("警告: 请务必按要求选择");
        System.out.println("程序不会检查选择的合法性,非法选择的请求将仍然被发送");
        System.out.println("选择入渠中,远征中,无船只或船只类型不符合要求(如1-6地图的要求)的舰队可能导致临时的或永久的封号");
        System.out.println();

        battledeck = new Deck(session, 1);
        for (int i=1; i<=4; i++) {
            try {
                battledeck.setDeck(i);
                System.out.println(battledeck);
            } catch (JSONException e) {}
        }
        System.out.println();
        System.out.print("选择出击舰队 (如1):");
        Scanner in = new Scanner(System.in);
        this.deck = in.nextInt();
        sortie();
    }

    private void sortie() throws Exception {
        System.out.println();
        System.out.println("出击");
        System.out.println("================");
        System.out.println("出击地图:   "+major_map+"-"+minor_map);
        battledeck.setDeck(deck);
        System.out.println("舰队: "+ battledeck);
        int i=0;
        for(Ship ship : battledeck.getShips()) {
            i++;
            System.out.println(""+i+"/"+ship+" 士气:"+ship.getCondition());
            System.out.println("HP: "+ship.getHp()+"/"+ship.getMaxhp()+" 火力: "+ship.getFirepower()+" 雷装: "+ship.getTorpedo()+" 对空: "+ship.getAntiair()+" 装甲: "+ship.getArmor());
        }
        System.out.println();
        System.out.print("请确认出击 (开始:Y):");
        Scanner in = new Scanner(System.in);
        if (in.next().equals("Y")) {
            beginmap();
        } else {
            System.out.println("出击已取消.");
        }
    }

    private void beginmap() throws Exception {
        System.out.println();
        System.out.println("战斗开始!");
        System.out.println();
        CustomRequest req = new CustomRequest(session.url, "api_req_map/start", "&api_formation=1&api_maparea_id="+major_map+"&api_mapinfo_no="+minor_map+"&api_deck_id="+deck);
        JSONObject data = req.request().getJSONObject("api_data");
        encounterNode(data);
    }

    private void encounterNode(JSONObject data) throws Exception {
        next = data.getInt("api_next");
        switch (data.getInt("api_event_id")) {
            case 4:
            case 5:
                System.out.println("* 遭遇战斗节点 " + (char) (data.getInt("api_no") - 1 + 'A'));
                battle();
                break;
            case 2:
                System.out.println("* 资源节点 " + (char) (data.getInt("api_no") - 1 + 'A'));
                System.out.print("获得:");
                switch (data.getJSONObject("api_itemget").getInt("api_id")) {
                    case 1:
                        System.out.print("燃料");
                        break;
                    case 2:
                        System.out.print("弹药");
                        break;
                    case 3:
                        System.out.print("钢材");
                        break;
                    case 4:
                        System.out.print("铝");
                        break;
                    default:
                        System.out.print(data.getJSONObject("api_itemget").getString("api_name")+"["+data.getJSONObject("api_itemget").getInt("api_id")+"]");
                }
                System.out.print("x" + data.getJSONObject("api_itemget").getInt("api_getcount"));
                advance();
                break;
            case 3:
                System.out.println("* 风暴节点 " + (char) (data.getInt("api_no") - 1 + 'A'));
                System.out.print("丢失:");
                switch (data.getJSONObject("api_happening").getInt("api_icon_id")) {
                    case 1:
                        System.out.print("燃料");
                        break;
                    case 2:
                        System.out.print("弹药");
                        break;
                    case 3:
                        System.out.print("钢材");
                        break;
                    case 4:
                        System.out.print("铝");
                        break;
                    default:
                        System.out.print("["+data.getJSONObject("api_itemget").getInt("api_id")+"]");
                }
                System.out.print("x"+data.getJSONObject("api_happening").getInt("api_count"));
                advance();
                break;
            default:
                System.out.println("Returned " + data.getInt("api_event_id"));
                System.out.print("未知节点. 请选择操作: (B=战斗,A=进击,other=撤退):");
                Scanner in = new Scanner(System.in);
                switch (in.next()) {
                    case "B":
                        battle();
                        break;
                    case "A":
                        advance();
                        break;
                    default:
                        next = 0;
                        advance();
                }
        }
    }

    private void battle() throws Exception {
        System.out.print("请选择阵型: (如单纵:1,单横:5):");
        Scanner in = new Scanner(System.in);
        String formation = in.next();
        if (formation.equals("0")) {
            System.out.println("已取消");
            return;
        }
        battle(formation);
    }

    private void battle(String formation) throws Exception {
        System.out.println();
        CustomRequest req = new CustomRequest(session.url, "api_req_sortie/battle", "&api_formation_id=" + formation + "&api_recovery_type=0");
        JSONObject data = req.request().getJSONObject("api_data");
        System.out.println();

        System.out.println("* 索敌");
//        System.out.println(data.getJSONArray("api_search"));
        JSONArray enemyfleet = data.getJSONArray("api_ship_ke");
        JSONArray enemyhp = data.getJSONArray("api_maxhps");
        JSONArray enemylv = data.getJSONArray("api_ship_lv");
        JSONArray enemyprops = data.getJSONArray("api_eParam");
        String[] enemynames = new String[6],
                 friendnames = new String[6];
        for (int i=1; i<=6&&enemyfleet.getInt(i)!=-1; i++) {
            enemynames[i-1]="("+i+")"+session.db.getShipName(enemyfleet.getInt(i));
            System.out.print(session.db.getShipName(enemyfleet.getInt(i)));
            System.out.println(" Lv." + enemylv.getInt(i) + " HP:" + enemyhp.getInt(i + 6));
            System.out.println("火力:"+enemyprops.getJSONArray(i-1).getInt(0)+" 雷装:"+enemyprops.getJSONArray(i-1).getInt(1)+" 对空:"+enemyprops.getJSONArray(i-1).getInt(2)+" 装甲:"+enemyprops.getJSONArray(i-1).getInt(3));
        }
        {
            int i=0;
            for (Ship ship : battledeck.getShips()) {
                friendnames[i]=ship.description;
                i++;
            }
        }
        JSONArray formations = data.getJSONArray("api_formation");
        switch (formations.getInt(0)) {
            case 1:
                System.out.print("单纵阵");
                break;
            case 2:
                System.out.print("复纵阵");
                break;
            case 3:
                System.out.print("轮型阵");
                break;
            case 4:
                System.out.print("梯形阵");
                break;
            case 5:
                System.out.print("单横阵");
                break;
            default:
                System.out.print(formations.getInt(0));
        }
        System.out.print(" - ");
        switch (formations.getInt(2)) {
            case 1:
                System.out.print("同航战");
                break;
            case 2:
                System.out.print("反航战");
                break;
            case 3:
                System.out.print("T字战-我舰队有利");
                break;
            case 4:
                System.out.print("T字战-我舰队不利");
                break;
            default:
                System.out.print(formations.getInt(2));
        }
        System.out.print(" - ");
        switch (formations.getInt(1)) {
            case 1:
                System.out.print("单纵阵");
                break;
            case 2:
                System.out.print("复纵阵");
                break;
            case 3:
                System.out.print("轮型阵");
                break;
            case 4:
                System.out.print("梯形阵");
                break;
            case 5:
                System.out.print("单横阵");
                break;
            default:
                System.out.print(formations.getInt(1));
        }
        System.out.println();
        Thread.sleep(1000);

        int[]   remainhp = new int[12],
                maxhp = new int[12];
        for (int i=1;i<=12;i++) {
            remainhp[i-1]=data.getJSONArray("api_nowhps").getInt(i);
            maxhp[i-1]=data.getJSONArray("api_maxhps").getInt(i);
        }

        System.out.println();
        System.out.println("* 航空战");
        try {
            JSONObject airbattle = data.getJSONObject("api_kouku");
            JSONObject stage1 = airbattle.getJSONObject("api_stage1");
            JSONObject stage2 = airbattle.getJSONObject("api_stage2");
            JSONObject stage3 = airbattle.getJSONObject("api_stage3");

            JSONArray fcv = airbattle.getJSONArray("api_plane_from").getJSONArray(0),
                      ecv = airbattle.getJSONArray("api_plane_from").getJSONArray(1);
            System.out.print("我航母: ");
            for (int i = 0; i<fcv.length()&&fcv.getInt(i)!=-1;i++) {
                System.out.print(battledeck.getShip(fcv.getInt(i)-1)+" ");
            }
            System.out.println();
            System.out.print("敌航母: ");
            for (int i = 0; i<fcv.length()&&ecv.getInt(i)!=-1;i++) {
                System.out.print(battledeck.getShip(ecv.getInt(i)-1)+" ");
            }
            System.out.println();

            System.out.println("我放出:" + stage1.get("api_f_count") + " 击毁:" + stage1.get("api_f_lostcount"));
            System.out.println("敌放出:" + stage1.get("api_e_count") + " 击毁:" + stage1.get("api_e_lostcount"));
            switch (stage1.getInt("api_disp_seiku")) {
                case 1:
                    System.out.println("制空权确保!");
                    break;
                case 2:
                    System.out.println("航空优势!");
                    break;
                case 3:
                    System.out.println("僵持!");
                    break;
                case 4:
                    System.out.println("航空劣势!");
                    break;
                case 5:
                    System.out.println("制空权丧失!");
                    break;
                default:
                    System.out.println("制空权未知:"+stage1.getInt("api_disp_seiku"));
            }

            System.out.println("我放出:"+stage2.get("api_f_count")+" 击落:"+stage2.get("api_f_lostcount"));
            System.out.println("敌放出:" + stage2.get("api_e_count") + " 击落:" + stage2.get("api_e_lostcount"));

            for (int i=0; i<6&&friendnames[i]!=null; i++) {
                int damage = stage3.getJSONArray("api_fdam").getInt(i+1);
                remainhp[i]-= damage;
                System.out.print(friendnames[i]+" "+remainhp[i]+"/"+maxhp[i]);
                boolean targeted = false;
                if (stage3.getJSONArray("api_fbak_flag").getInt(i+1)!=0) {
                    System.out.print(" (被轰炸)");
                    targeted = true;
                }
                if (stage3.getJSONArray("api_frai_flag").getInt(i+1)!=0) {
                    System.out.print(" (被雷击)");
                    targeted = true;
                }
                if (targeted) {
                    if (damage==0) {
                        System.out.print(" miss");
                    } else {
                        System.out.print(" -"+damage);
                    }
                }
                System.out.println();
            }
            for (int i=0; i<6&&enemynames[i]!=null; i++) {
                int damage = stage3.getJSONArray("api_edam").getInt(i+1);
                remainhp[i+6]-= damage;
                System.out.print(enemynames[i]+" "+remainhp[i+6]+"/"+maxhp[i+6]);
                boolean targeted = false;
                if (stage3.getJSONArray("api_ebak_flag").getInt(i+1)!=0) {
                    System.out.print(" (被轰炸)");
                    targeted = true;
                }
                if (stage3.getJSONArray("api_erai_flag").getInt(i+1)!=0) {
                    System.out.print(" (被雷击)");
                    targeted = true;
                }
                if (targeted) {
                    if (damage==0) {
                        System.out.print(" miss");
                    } else {
                        System.out.print(" -"+damage);
                    }
                }
                System.out.println();
            }

        } catch (JSONException e) {
            System.out.println("N/A");
        } catch (Exception e) {
            e.printStackTrace();
        }
        Thread.sleep(1000);


        System.out.println();
        System.out.println("* 开幕雷击");
        try {
            JSONObject opatk = data.getJSONObject("api_opening_atack");

            JSONArray fcall = opatk.getJSONArray("api_fcl");
            JSONArray ftarget = opatk.getJSONArray("api_frai");
            JSONArray fdealt = opatk.getJSONArray("api_fydam");
            for (int i=1;i<=6;i++) {
                if (fcall.getInt(i)!=0) {
                    System.out.print(friendnames[i-1]+" ==> ");
                    System.out.print(enemynames[ftarget.getInt(i)-1]);
                    int damage = (int)fdealt.getDouble(i);
                    if (damage==0) {
                        System.out.print(" miss");
                    } else {
                        System.out.print(" -"+damage);
                    }
                    System.out.println();
                }
            }

            JSONArray ecall = opatk.getJSONArray("api_ecl");
            JSONArray etarget = opatk.getJSONArray("api_erai");
            JSONArray edealt = opatk.getJSONArray("api_eydam");
            for (int i=1;i<=6;i++) {
                if (ecall.getInt(i)!=0) {
                    System.out.print(enemynames[i-1]+" ==> ");
                    System.out.print(friendnames[etarget.getInt(i)-1]);
                    int damage = (int)edealt.getDouble(i);
                    if (damage==0) {
                        System.out.print(" miss");
                    } else {
                        System.out.print(" -"+damage);
                    }
                    System.out.println();
                }
            }

            System.out.println("----------------");
            for (int i=1;i<=6&&friendnames[i-1]!=null;i++) {
                int damage = (int)opatk.getJSONArray("api_fdam").getDouble(i);
                remainhp[i-1]-=damage;
                System.out.println(friendnames[i - 1] + " " + remainhp[i - 1]+"/"+maxhp[i-1]);
            }
            for (int i=1;i<=6&&enemynames[i-1]!=null;i++) {
                int damage = (int)opatk.getJSONArray("api_edam").getDouble(i);
                remainhp[i-1+6]-=damage;
                System.out.println(enemynames[i - 1] + " " + remainhp[i + 6 - 1] + "/"+maxhp[i+6-1]);
            }
            System.out.println("----------------");
            System.out.println();
        } catch (JSONException e) {
            System.out.println("N/A");
        } catch (Exception e) {
            e.printStackTrace();
        }
        Thread.sleep(1000);

        for (int hougeki_idx = 1; hougeki_idx<=3; hougeki_idx++) {
            System.out.println();
            System.out.println("* 炮击战-" + hougeki_idx);
            try {
                JSONObject hougeki = data.getJSONObject("api_hougeki"+hougeki_idx);
                JSONArray attack = hougeki.getJSONArray("api_at_list");
                JSONArray target = hougeki.getJSONArray("api_df_list");
                JSONArray equip = hougeki.getJSONArray("api_si_list");
                JSONArray dealt = hougeki.getJSONArray("api_damage");
                JSONArray type = hougeki.getJSONArray("api_at_type");
                for (int i=1; i<attack.length(); i++){
                    int attacker = attack.getInt(i);
                    if (attack.getInt(i)<=6) {
                        System.out.print(friendnames[attack.getInt(i)-1]+" ");
                    } else {
                        System.out.print(enemynames[attack.getInt(i)-1-6]+" ");
                    }
                    switch (type.getInt(i)) {
                        case 0:
                        case 2:
                            break;
                        case 3:
                            System.out.print("弹着观测射击! ");
                            break;
                        default:
                            System.out.print("["+type.getInt(i)+"]");
                    }
                    JSONArray equips = equip.getJSONArray(i);
                    for (int j=0; j<equips.length()&&equips.getInt(j)!=-1;j++) {
                        System.out.print(session.db.getEquipName(equips.getInt(j))+" ");
                    }
                    System.out.print("===> ");
                    if (target.getJSONArray(i).getInt(0)<=6) {
                        System.out.print(friendnames[target.getJSONArray(i).getInt(0)-1]+" ");
                    } else {
                        System.out.print(enemynames[target.getJSONArray(i).getInt(0)-1-6]+" ");
                    }
                    JSONArray damage = dealt.getJSONArray(i);
                    for (int k=0; k<damage.length(); k++) {
                        remainhp[target.getJSONArray(i).getInt(0)-1]-=damage.getInt(k);
                        if (damage.getInt(k) == 0) {
                            System.out.print("miss ");
                        } else {
                            System.out.print("-" + damage.getInt(k) + " ");
                        }
                        System.out.println();
                    }
                }
            } catch (JSONException e) {
                System.out.println("N/A");
            } catch (Exception e) {
                e.printStackTrace();
            }
            Thread.sleep(1000);
        }

        System.out.println("----------------");
        for (int i=1;i<=6&&friendnames[i-1]!=null;i++) {
            System.out.println(friendnames[i - 1] + " " + remainhp[i - 1]+"/"+maxhp[i-1]);
        }
        for (int i=1;i<=6&&enemynames[i - 1]!=null;i++) {
            System.out.println(enemynames[i - 1] + " " + remainhp[i + 6 - 1] + "/"+maxhp[i+6-1]);
        }
        System.out.println("----------------");
        Thread.sleep(1000);

        System.out.println();
        System.out.println("* 闭幕雷击");
        try {
            JSONObject edatk = data.getJSONObject("api_raigeki");

            JSONArray fcall = edatk.getJSONArray("api_fcl");
            JSONArray ftarget = edatk.getJSONArray("api_frai");
            JSONArray fdealt = edatk.getJSONArray("api_fydam");
            for (int i=1;i<=6;i++) {
                if (fcall.getInt(i)!=0) {
                    System.out.print(friendnames[i-1]+" ==> ");
                    System.out.print(enemynames[ftarget.getInt(i)-1]);
                    int damage = (int)fdealt.getDouble(i);
                    if (damage==0) {
                        System.out.print(" miss");
                    } else {
                        System.out.print(" -"+damage);
                    }
                    System.out.println();
                }
            }

            JSONArray ecall = edatk.getJSONArray("api_ecl");
            JSONArray etarget = edatk.getJSONArray("api_erai");
            JSONArray edealt = edatk.getJSONArray("api_eydam");
            for (int i=1;i<=6;i++) {
                if (ecall.getInt(i)!=0) {
                    System.out.print(enemynames[i-1]+" ==> ");
                    System.out.print(friendnames[etarget.getInt(i)-1]);
                    int damage = (int)edealt.getDouble(i);
                    if (damage==0) {
                        System.out.print(" miss");
                    } else {
                        System.out.print(" -"+damage);
                    }
                    System.out.println();
                }
            }

            System.out.println("----------------");
            for (int i=1;i<=6&&friendnames[i-1]!=null;i++) {
                int damage = (int)edatk.getJSONArray("api_fdam").getDouble(i);
                remainhp[i-1]-=damage;
                System.out.println(friendnames[i - 1] + " " + remainhp[i - 1] + "/" + maxhp[i - 1]);
            }
            for (int i=1;i<=6&&enemynames[i-1]!=null;i++) {
                int damage = (int)edatk.getJSONArray("api_edam").getDouble(i);
                remainhp[i-1+6]-=damage;
                System.out.println(enemynames[i - 1] + " " + remainhp[i + 6 - 1] + "/" + maxhp[i + 6 - 1]);
            }
            System.out.println("----------------");
            System.out.println();
        } catch (JSONException e) {
            System.out.println("N/A");
        } catch (Exception e) {
            e.printStackTrace();
        }
        Thread.sleep(500);

        if (data.getInt("api_midnight_flag")!=0) {
            System.out.print("* 离脱判定 (Y=夜战突入):");
            Scanner in = new Scanner(System.in);
            if (in.next().equals("Y")) {
                nightBattle(friendnames, enemynames, remainhp, maxhp);
            }
        }
        battleResult();

    }

    private void nightBattle(String[] friendnames, String[] enemynames, int[] remainhp, int[] maxhp) throws Exception {
        CustomRequest req = new CustomRequest(session.url, "api_req_battle_midnight/battle", "&api_recovery_type=0");
        JSONObject data = req.request().getJSONObject("api_data");
        System.out.println("* 夜战");
        try {
            JSONObject hougeki = data.getJSONObject("api_hougeki");
            JSONArray attack = hougeki.getJSONArray("api_at_list");
            JSONArray target = hougeki.getJSONArray("api_df_list");
            JSONArray equip = hougeki.getJSONArray("api_si_list");
            JSONArray dealt = hougeki.getJSONArray("api_damage");
//            JSONArray type = hougeki.getJSONArray("api_at_type");
            for (int i=1; i<attack.length(); i++){
                int attacker = attack.getInt(i);
                if (attack.getInt(i)<=6) {
                    System.out.print(friendnames[attack.getInt(i)-1]+" ");
                } else {
                    System.out.print(enemynames[attack.getInt(i)-1-6]+" ");
                }
                /*
                switch (type.getInt(i)) {
                    case 0:
                    case 2:
                        break;
                    case 3:
                        System.out.print("弹着观测射击! ");
                        break;
                    default:
                        System.out.print("["+type.getInt(i)+"]");
                }
                */
                JSONArray equips = equip.getJSONArray(i);
                for (int j=0; j<equips.length()&&equips.getInt(j)!=-1;j++) {
                    System.out.print(session.db.getEquipName(equips.getInt(j))+" ");
                }
                System.out.print("===> ");
                if (target.getJSONArray(i).getInt(0)<=6) {
                    System.out.print(friendnames[target.getJSONArray(i).getInt(0)-1]+" ");
                } else {
                    System.out.print(enemynames[target.getJSONArray(i).getInt(0)-1-6]+" ");
                }
                JSONArray damage = dealt.getJSONArray(i);
                for (int k=0; k<damage.length(); k++) {
                    remainhp[target.getJSONArray(i).getInt(0)-1]-=damage.getInt(k);
                    if (damage.getInt(k) == 0) {
                        System.out.print("miss ");
                    } else {
                        System.out.print("-" + damage.getInt(k) + " ");
                    }
                    System.out.println();
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        System.out.println("----------------");
        for (int i=1;i<=6&&friendnames[i-1]!=null;i++) {
            System.out.println(friendnames[i - 1] + " " + remainhp[i - 1]+"/"+maxhp[i-1]);
        }
        for (int i=1;i<=6&&enemynames[i - 1]!=null;i++) {
            System.out.println(enemynames[i - 1] + " " + remainhp[i + 6 - 1] + "/"+maxhp[i+6-1]);
        }
        System.out.println("----------------");
        Thread.sleep(5000);
    }

    private void battleResult() throws Exception {
        // Result
        CustomRequest req = new CustomRequest(session.url, "api_req_sortie/battleresult", "");
        JSONObject data = req.request().getJSONObject("api_data");
        System.out.println("战斗结果: " + data.getString("api_quest_name") + " " + data.getJSONObject("api_enemy_info").getString("api_deck_name"));
        System.out.println("================");
        System.out.println("RANK: "+data.getString("api_win_rank"));
        System.out.println();
        System.out.println("MVP: "+data.getInt("api_mvp")+" BaseXP: "+data.getInt("api_get_base_exp"));
        System.out.println();
        fleet.update();
        Deck deck = new Deck(session, this.deck);
        int i = 0;
        for (Ship ship : deck.getShips()) {
            i++;
            System.out.print("Ship " + i + " EXP+" + data.getJSONArray("api_get_ship_exp").getInt(i)); // Data returned indexes from 1 to 6
            System.out.print(" ("+data.getJSONArray("api_get_exp_lvup").getJSONArray(i-1).getInt(0)+"/"+data.getJSONArray("api_get_exp_lvup").getJSONArray(i-1).getInt(1)+")");
            System.out.print(" Lv."+ship.getLv());
            System.out.print(" HP: "+ship.getHp()+"/"+ship.getMaxhp());
            System.out.println();
        }
        System.out.println();
        if (data.getJSONArray("api_get_flag").getInt(1)!=0) {
            // Get new ship
            System.out.println("+ 发现新船只: "+data.getJSONObject("api_get_ship").getString("api_ship_name"));
            System.out.println(data.getJSONObject("api_get_ship").getString("api_ship_getmes"));
            System.out.println();
        }

        advance();
    }

    private void advance() throws Exception {
        if (next!=0) {
            System.out.println();
            System.out.print("战斗结束 (Y=进击):");
            Scanner in = new Scanner(System.in);
            if (in.next().equals("Y")) {
                CustomRequest req = new CustomRequest(session.url, "api_req_map/next", "&api_recovery_type=0");
                JSONObject data = req.request().getJSONObject("api_data");
                encounterNode(data);
            }
        } else {
            System.out.println("出击结束.");
            System.out.println("请立即刷新母港.");
            System.out.println("如果你在刷新母港前进行补给,你将会猫");
        }
    }

    public static void battleDelay(JSONObject data) throws Exception{
//        System.out.println(data.getJSONArray("api_search"));
        JSONArray enemyfleet = data.getJSONArray("api_ship_ke");
        JSONArray enemyhp = data.getJSONArray("api_maxhps");
        JSONArray enemylv = data.getJSONArray("api_ship_lv");
        JSONArray enemyprops = data.getJSONArray("api_eParam");
        String[] enemynames = new String[6],
                friendnames = new String[6];
        JSONArray formations = data.getJSONArray("api_formation");

        // Announce Formations
        Thread.sleep(5000);

        // Aerial Combat
        try {
            JSONObject airbattle = data.getJSONObject("api_kouku");
            JSONObject stage1 = airbattle.getJSONObject("api_stage1");
            JSONObject stage2 = airbattle.getJSONObject("api_stage2");
            JSONObject stage3 = airbattle.getJSONObject("api_stage3");

            Thread.sleep(10000);
        } catch (JSONException e) {
        } catch (Exception e) {
            e.printStackTrace();
        }


        try {
            // Opening salvo
            JSONObject opatk = data.getJSONObject("api_opening_atack");

            JSONArray fcall = opatk.getJSONArray("api_fcl");
            JSONArray ftarget = opatk.getJSONArray("api_frai");
            JSONArray fdealt = opatk.getJSONArray("api_fydam");

            Thread.sleep(5000);
        } catch (JSONException e) {
            System.out.println("N/A");
        } catch (Exception e) {
            e.printStackTrace();
        }

        for (int hougeki_idx = 1; hougeki_idx<=3; hougeki_idx++) {
            // Shelling
            try {
                JSONObject hougeki = data.getJSONObject("api_hougeki"+hougeki_idx);
                JSONArray attack = hougeki.getJSONArray("api_at_list");
                JSONArray target = hougeki.getJSONArray("api_df_list");
                JSONArray equip = hougeki.getJSONArray("api_si_list");
                JSONArray dealt = hougeki.getJSONArray("api_damage");
                JSONArray type = hougeki.getJSONArray("api_at_type");
                for (int i=1; i<attack.length(); i++){
                    Thread.sleep(3000);
                }
            } catch (JSONException e) {
                System.out.println("N/A");
            } catch (Exception e) {
                e.printStackTrace();
            }
            Thread.sleep(1000);
        }

        try {
            // Closing salvo

            JSONObject edatk = data.getJSONObject("api_raigeki");

            JSONArray fcall = edatk.getJSONArray("api_fcl");
            JSONArray ftarget = edatk.getJSONArray("api_frai");
            JSONArray fdealt = edatk.getJSONArray("api_fydam");
            Thread.sleep(4000);
        } catch (JSONException e) {
            System.out.println("N/A");
        } catch (Exception e) {
            e.printStackTrace();
        }

        // Random amount correction
        Thread.sleep((int)(10000*(Math.pow(Math.random(),2))));
    }

    public static void nightBattleDelay(JSONObject data) throws Exception {
        System.out.println("* 夜战");
        try {
            JSONObject hougeki = data.getJSONObject("api_hougeki");
            JSONArray attack = hougeki.getJSONArray("api_at_list");
            JSONArray target = hougeki.getJSONArray("api_df_list");
            JSONArray equip = hougeki.getJSONArray("api_si_list");
            JSONArray dealt = hougeki.getJSONArray("api_damage");
//            JSONArray type = hougeki.getJSONArray("api_at_type");
            for (int i=1; i<attack.length(); i++){
                Thread.sleep(2500);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        // Random amount correction
        Thread.sleep((int)(10000*(Math.pow(Math.random(), 2))));

    }

}
