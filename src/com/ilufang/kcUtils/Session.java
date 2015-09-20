package com.ilufang.kcUtils;

import com.ilufang.kcInteractive.*;
import com.ilufang.kcLogging.BuildLogging;
import com.ilufang.kcLogging.DropLogging;
import com.ilufang.kcLogging.KCLogger;
import com.ilufang.kcRequest.*;
import org.json.*;

import java.io.File;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by ilufang on 4/21/15.
 */
public class Session {
    public final String url;

    public ShipsDB db;
    public Fleet fleet;
    public Arsenal arsenal;

    public JSONObject user;
    public final String username;

    public List<KCLogger> loggers;

    public Session(String url) throws Exception {
        this.url = url;
        System.out.println("  Building database...");
        db = new ShipsDB(this);
        System.out.println("  Fetching info...");
        fleet = new Fleet(this);
        arsenal = new Arsenal(this);
        CustomRequest req = new CustomRequest(url, "api_get_member/basic", "");
        user = req.request().getJSONObject("api_data");
        username = user.getString("api_nickname");

        loggers = new ArrayList<>();
        loggers.add(new DropLogging());
        loggers.add(new BuildLogging(this));
    }



    public boolean parseCmd(String cmd) throws Exception {
        String[] args = cmd.split(" ");
        if (args.length == 0) {
            return true;
        }
        double xuan;
        int index;
        switch (args[0]) {
            case "?":
                System.out.println("? This message");
                System.out.println("1-5 半自动1-5肝船(单船A点循环)");
                System.out.println("battle 命令行出击!");
                System.out.println("req 发送自定义请求");
                System.out.println("update 更新舰队信息");
                System.out.println("updatedb 更新数据库");
                System.out.println("build 建造");
                System.out.println("instabuild 高速建造");
                System.out.println("batchbuild 批量建造");
                System.out.println("peekbuild 查看建造槽内容");
                System.out.println("printdb 查看shipsdb.json数据库内容");
                System.out.println("exportdb 导出shipsdb.json数据库内容为csv表格");
                System.out.println("download 下载指定id船只的swf资源");
                System.out.println("sparkle1-1 1-1刷闪(非DD/CL)");
                System.out.println("sparkle1-5 1-5刷闪(DD/CL)");
                break;
            case "sparkle1-1":
                if (args.length<3) {
                    System.out.println("sparkle1-1 <deck> <slot>");
                    return true;
                }
                new Sparkle(this).sparkle(Integer.parseInt(args[1]), Integer.parseInt(args[2]), "1", true);
                break;
            case "sparkle1-5":
                if (args.length<3) {
                    System.out.println("sparkle1-5 <deck> <slot>");
                    return true;
                }
                new Sparkle(this).sparkle(Integer.parseInt(args[1]), Integer.parseInt(args[2]), "5", false);
                break;
            case "battle":
                Battle battle = new Battle(this);
                break;
            case "1-5":
                Battle15 battle15 = new Battle15(this);
                break;
            case "develop":
                if (args.length < 5) {
                    System.out.println("develop <fuel> <bullet> <steel> <bauxite>");
                    return true;
                }
                arsenal.develop(Integer.parseInt(args[1]), Integer.parseInt(args[2]), Integer.parseInt(args[3]), Integer.parseInt(args[4]));
                break;
            case "batchdev":
                if (args.length<6) {
                    System.out.println("batchdev <count> <fuel> <bullet> <steel> <bauxite>");
                    break;
                }
                arsenal.developbatch(Integer.parseInt(args[1]), Integer.parseInt(args[2]), Integer.parseInt(args[3]), Integer.parseInt(args[4]), Integer.parseInt(args[5]));
                break;
            case "build":
                if (args.length < 6) {
                    System.out.println("build <slot> <fuel> <bullet> <steel> <bauxite> [xuan-coeff]");
                    break;
                }
                xuan = 0.0;
                if (args.length >= 7) {
                    xuan = Integer.parseInt(args[6]) / 100.0;
                }
                arsenal.build(Integer.parseInt(args[1]), Integer.parseInt(args[2]), Integer.parseInt(args[3]), Integer.parseInt(args[4]), Integer.parseInt(args[5]), xuan);
                break;
            case "instabuild":
                if (args.length < 6) {
                    System.out.println("instabuild <slot> <fuel> <bullet> <steel> <bauxite> [xuan-coeff]");
                    break;
                }
                xuan = 0.0;
                if (args.length >= 7) {
                    xuan = Integer.parseInt(args[6]) / 100.0;
                }
                arsenal.buildinstant(Integer.parseInt(args[1]), Integer.parseInt(args[2]), Integer.parseInt(args[3]), Integer.parseInt(args[4]), Integer.parseInt(args[5]), xuan);
                break;
            case "batchbuild":
                if (args.length < 7) {
                    System.out.println("batchbuild <slot> <count> <fuel> <bullet> <steel> <bauxite> [xuan-coeff]");
                    break;
                }
                xuan = 0.0;
                if (args.length >= 8) {
                    xuan = Integer.parseInt(args[7]) / 100.0;
                }
                arsenal.buildbatch(Integer.parseInt(args[1]), Integer.parseInt(args[2]), Integer.parseInt(args[3]), Integer.parseInt(args[4]), Integer.parseInt(args[5]), Integer.parseInt(args[6]), xuan);
                break;
            case "peekbuild":
                if (args.length < 2) {
                    System.out.println("peekbuild <slot>");
                    break;
                }
                System.out.println(arsenal.get(Integer.parseInt(args[1])));
                break;
            case "update":
                fleet.update();
                break;
            case "updatedb":
                File file=new File("./shipsdb.json");
                file.delete();
                System.out.println("Reloading database...");
                db = new ShipsDB(this);
                break;
            case "printdb":
                db.printDB();
                break;
            case "exportdb":
                db.exportCSV();
                break;
            case "download":
                db.fetchShipSwf(Integer.parseInt(args[1]));
                break;
            case "destroyAll":
                DestroyAll destroy = new DestroyAll(this);
                destroy.destroy();
                break;
            case "req":
                CustomRequest req;
                if (args.length == 1) {
                    System.out.println("req <api_path> [post_args]");
                    break;
                } else if (args.length == 2) {
                    req = new CustomRequest(url, args[1], "");
                } else {
                    req = new CustomRequest(url, args[1], args[2]);
                }
                System.out.println(req.request());
                break;
            case "exit":
                return false;
            default:
                System.out.println(args[0] + ": Command not found.");
        }
        return true;
    }

    public String cmdPrompt() {
        return "KancolleTerminal: "+username+" $ ";
    }

}
