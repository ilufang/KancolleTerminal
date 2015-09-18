package com.ilufang.kcLogging;

import org.json.JSONObject;

/**
 * Created by root on 5/13/15.
 */
public interface KCLogger {

    // Receives packet from local delegate server
    void processRequest(String url, String request, String response);
    JSONObject getLog();
}
