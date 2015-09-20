package com.ilufang.kcInteractive;

import com.ilufang.kcRequest.CustomRequest;
import com.ilufang.kcRequest.KCRequest;
import com.ilufang.kcUtils.Session;
import com.ilufang.kcUtils.Ship;

import java.util.Scanner;

/**
 * Created by ilufang on 9/20/15.
 */
public class DestroyAll {
    private Session session;

    public DestroyAll(Session session) {
        this.session = session;
    }

    public void destroy() {
        System.out.println("!!!WARNING!!!");
        System.out.println("This will destroy ALL your available ships!");
        System.out.println("Except Secretary, docking and expedition ships");
        System.out.println("INCLUDING LOCKED");
        System.out.println("THIS ACTION CANNOT BE UNDONE");
        System.out.println();
        System.out.print("Destroy all ships? (Y/n):");
        Scanner in = new Scanner(System.in);
        if (in.next().equals("Y")) {
            System.out.println();
            try {
                startDestroy();
                System.out.println("Ships destroyed.");
            } catch (Exception e) {
                System.out.println("A fatal error occurred:");
                e.printStackTrace();
            }
        } else {
            System.out.println("Destroy Cancelled. No ships are affected.");
        }
    }

    private void startDestroy() throws Exception {
        System.out.println("Preparing ships...");
        session.fleet.forceUpdate();
        System.out.println(session.fleet.getShips().size() + "Ships loaded.");
        for (Ship ship : session.fleet.getShips()) {
            try {
                destroyShip(ship);
                System.out.println("OK");
            } catch (Exception e) {
                System.out.println("Error");
                e.printStackTrace();
            }
        }
        session.fleet.forceUpdate();
        System.out.println(session.fleet.getShips().size() + "Ships remaining.");
    }

    private void destroyShip(Ship ship) throws Exception{
        System.out.print(ship.getId()+" "+ship+"...");
        if (ship.isLocked()) {
            // Unlock
            CustomRequest unlock = new CustomRequest(session.url, "api_req_hensei/lock", "&api_ship_id="+ship.getId());
            unlock.request();
        }
        System.out.print(".");
        try {
            CustomRequest unsetall = new CustomRequest(session.url, "api_req_kaisou/unsetslot_all", "&api_id=" + ship.getId());
            unsetall.request();
        } catch (Exception e) {
            // Ignore if ship does not have equipment to unset
        }
        System.out.print(".");
        CustomRequest destroy = new CustomRequest(session.url, "api_req_kousyou/destroyship", "&api_ship_id="+ship.getId());
        destroy.request();
        System.out.print(".");
    }
}
