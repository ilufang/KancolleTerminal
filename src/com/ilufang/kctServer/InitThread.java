package com.ilufang.kctServer;

import com.ilufang.kcUtils.Session;

/**
 * Created by ilufang on 5/5/15.
 */
public class InitThread extends Thread {
    public boolean done,error;
    public Session session;
    private String url;
    public Exception exception;
    public InitThread(String url) {
        done = false;
        error = false;
        this.url = url;
    }
    public void run() {
        try {
            session = new Session(url);
            done = true;
        } catch (Exception e) {
            this.exception = e;
            error = true;
        }
    }
}
