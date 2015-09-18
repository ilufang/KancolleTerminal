package com.ilufang.kcInputDaemon;

import com.ilufang.kcRequest.CustomRequest;

import javax.imageio.ImageIO;
import javax.imageio.ImageWriter;
import javax.imageio.stream.ImageOutputStream;
import java.awt.*;
import java.awt.event.InputEvent;
import java.awt.image.BufferedImage;
import java.io.File;
import java.net.URI;
import java.util.Iterator;

/**
 * Created by ilufang on 5/10/15.
 */
public class InputDaemon {
    private int x0, y0,x1, y1; // Swf Bounding box
    private int w, h;
    Robot robot;
    Exception exception;

    public InputDaemon() throws Exception {
        robot = new Robot();
    }

    public void setBound(int x0, int y0, int x1, int y1) {
        this.x0=x0;
        this.x1=x1;
        this.y0=y0;
        this.y1=y1;
        w = x1-x0;
        h = y1-y0;
    }

    public void openURL(String url) throws Exception {
        Desktop.getDesktop().browse(new URI(url));
    }

    public void clickAbs(int x, int y) {
        robot.mouseMove(x, y);
        robot.mousePress(InputEvent.BUTTON1_MASK);
        robot.mouseRelease(InputEvent.BUTTON1_MASK);
    }

    public void click(double x, double y) {
        clickAbs((int) (x0 + w * x), (int) (y0 + h * y));
    }

    public void screenCap() throws Exception {
        BufferedImage img = robot.createScreenCapture(new Rectangle(x0,y0,w,h));
        Iterator writers = ImageIO.getImageWritersByFormatName("png");
        ImageWriter writer = (ImageWriter)writers.next();
        File f = new File("screencap.png");
        ImageOutputStream ios = ImageIO.createImageOutputStream(f);
        writer.setOutput(ios);
        writer.write(img);
    }

    private String url; // TODO sketchy implementation
    public void port(String url) throws Exception{
        // Only call when reaching end-of-battle
        // Send retreat sequence
        // slotitem, unsetslot, useitem
        this.url = url;
        Thread.sleep(500);
        CustomRequest req = new CustomRequest(url, "api_get_member/slot_item", "");
        req.request();
        Thread.sleep(500);
        req = new CustomRequest(url, "api_get_member/unsetslot", "");
        req.request();
        Thread.sleep(500);
        req = new CustomRequest(url, "api_get_member/useitem", "");
        req.request();
        Thread.sleep(500);

        /*
req api_get_member/slot_item
req api_get_member/unsetslot
req api_get_member/useitem
        */
        // Finally, port!
        generatePort();
    }

    public boolean port_received, dock_received, port_finished;

    private void generatePort() {
        port_received = false;
        dock_received = false;
        port_finished = false;
        Thread listener = new PortRequestListener(this);
        listener.start();
        final double
                port_x = 0.059148,
                port_y = 0.086680;
        click(port_x, port_y);
        // Break. Free main server so that the actual port request can be handled.
    }

    public void portFinalize() {
        try {
            Thread.sleep(1000);
            CustomRequest req = new CustomRequest(url, "api_get_member/questlist", "&api_page_no=1");
            req.request();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

}
