package com.ilufang.kcLogging;

import org.json.*;

import java.io.*;
import java.nio.charset.Charset;

/**
 * Created by root on 5/13/15.
 */
public abstract class KCLogging implements KCLogger {
    protected String logfile;
    protected JSONObject log;
    public JSONObject getLog() {
        return log;
    }
    public void saveLog() throws IOException {
        File file = new File(logfile);
        if (!file.exists()) {
            file.createNewFile();
        } else {
            file.delete();
            file.createNewFile();
        }
        FileOutputStream os = new FileOutputStream(file);
        os.write(log.toString().getBytes(Charset.forName("utf-8")));
        os.close();
    }

    public void loadLog() throws IOException{
        File file = new File(logfile);
        if (file.exists()) {
            FileInputStream is = new FileInputStream(file);
            BufferedReader reader = new BufferedReader(new InputStreamReader(is, Charset.forName("utf-8")));
            String data="", line;
            while ((line = reader.readLine())!=null) {
                data += line+'\n';
            }
            log = new JSONObject(data);
        } else {
            log = new JSONObject();
        }
    }

}
