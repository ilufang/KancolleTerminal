package com.ilufang.kcInputDaemon;

import com.ilufang.kcRequest.CustomRequest;

/**
 * Created by root on 5/12/15.
 */
public class PortRequestListener extends Thread {
    InputDaemon kcid;
    public PortRequestListener(InputDaemon master) {
        kcid=master;
    }
    public void run() {
        final double
                dock_x = 0.135757,
                dock_y = 0.751227;

        try {
            // 1. Wait for the port command
            while (!kcid.port_received) {
                Thread.sleep(100);
            }
            // 2. Switch to other screen
            while (!kcid.dock_received) {
                kcid.click(dock_x, dock_y);
                Thread.sleep(500);
            }
            kcid.portFinalize();
            kcid.port_finished = true;
        } catch (Exception e) {
            e.printStackTrace();
        }

    }
}
