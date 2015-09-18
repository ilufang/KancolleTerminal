package com.ilufang.kcRequest;

import javax.net.ssl.HttpsURLConnection;
import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.Charset;

/**
 * Created by ilufang on 4/17/15.
 */
public abstract class KCRequest {
    protected final String api_token;
    protected final String server;
    protected final URL url_from;
    protected String api_path;
    protected String args;

    public KCRequest(String url) throws Exception{
        url_from = new URL(url);
        server = url_from.getHost();
        api_path = "kcsapi"; // Subclasses should append paths

        // extract api_token
        String trait = "api_token=";
        int idx = url.indexOf(trait)+trait.length();
        trait = "&";
        int idx_end = url.indexOf(trait, idx);
        api_token = url.substring(idx, idx_end);

        args = "api_verno=1&api_token="+api_token; // Subclasses should append args
    }

    public String connect() throws Exception{
        URL requrl = new URL(String.format("http://%s/%s", server, api_path));
        HttpURLConnection con = (HttpURLConnection)requrl.openConnection();


        // Configure Connection
        byte[] postData = args.replace("_","%5F").getBytes(Charset.forName("UTF-8"));
        int postDataLength = postData.length;
        con.setDoOutput(true);
        con.setDoInput(true);
        con.setRequestMethod("POST");
        con.setRequestProperty("User-Agent", "Mozilla/5.0 (Macintosh; Intel Mac OS X 10_9_4) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/41.0.2272.118 Safari/537.36");
        con.setRequestProperty("Content-Type", "application/x-www-form-urlencoded");
        con.setRequestProperty("Charset", "utf-8");
        con.setRequestProperty("Accept", "*/*"); // TODO FIX COMMENT!!!
        con.setRequestProperty("Content-Length", Integer.toString( postDataLength));
        con.setRequestProperty("X-Requested-With", "ShockwaveFlash/17.0.0.134");
        con.setRequestProperty("Referer", url_from+"/[[DYNAMIC]]/1");
        con.setRequestProperty("Accept-Encoding", "gzip, deflate");
        con.setRequestProperty("Accept-Language", "en-US,en;q=0.8,ja;q=0.6,zh;q=0.4,zh-TW,q=0.2");

        // Write Post data
        DataOutputStream wr = new DataOutputStream(con.getOutputStream());
        wr.write(postData);

        // Retrive Info
        BufferedReader in = new BufferedReader(new InputStreamReader(con.getInputStream()));
        String inputLine, returnedData="";
        while ((inputLine = in.readLine()) != null) {
            returnedData += inputLine;
        }
        in.close();
        return returnedData;
    }

    public String toString() {
        return "KCReq:"+api_path;
    }
}
