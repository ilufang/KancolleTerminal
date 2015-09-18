import com.ilufang.kcLogging.DropLogging;
import com.ilufang.kcRequest.CustomRequest;
import com.ilufang.kcUtils.*;
import com.ilufang.kctServer.Server;
import org.json.*;

import java.util.Date;
import java.util.Scanner;

/**
 * Created by ilufang on 4/18/15.
 */
public class Main {
    public static void shellMain(String[] args) throws Exception {
        Session session = promptURL();
        System.out.println();
        System.out.println("Kancolle Terminal");
        System.out.println("(c) 2015 by ilufang");
        System.out.println();
        System.out.println("This software comes with ABSOLUTELY NO WARRANTY. Use at your own risk.");
        System.out.println();
        String mode;
        Scanner in = new Scanner(System.in);
        if (args.length<=1) {
            while (true) {
                System.out.print(session.cmdPrompt());
                try {
                    if (!session.parseCmd(in.nextLine())) {
                        return;
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
                System.out.println();
            }
        } else {
            //session.parseCmd(String.join(" ", args));
        }
        System.out.println("Logout.");
    }

    public static void serverMain(String[] args) throws Exception {
        if (args.length<=1) {
            Server.beginServer();
        } else {
            Server.beginServer(Integer.parseInt(args[1]));
        }
    }

    public static void main0(String[] args) throws Exception {
        ShipsDB db = new ShipsDB(null);
        Scanner in = new Scanner(System.in);
        while(true) {
            System.out.println(db.getEquipName(in.nextInt()));
        }
    }

    public static void main(String[] args) throws Exception{
        if (args.length==0) {
            // default goes to server
            serverMain(args);
        } else {
            switch (args[0]) {
                case "server":
                    serverMain(args);
                    break;
                case "shell":
                    shellMain(args);
                    break;
                default:
                    serverMain(args);
            }
        }
    }

    private static Session promptURL() throws Exception {
        System.out.print("SWF URL:");
        Scanner in = new Scanner(System.in);
        String url = in.next();
        System.out.println("Logging in...");
        return new Session(url);
    }
}
