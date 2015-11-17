package com.pellcorp.android.flixbmc.jsonrpc;

import android.util.Base64;
import com.thetransactioncompany.jsonrpc2.client.ConnectionConfigurator;
import java.net.HttpURLConnection;

public class BasicAuthenticator implements ConnectionConfigurator {

    private String user;
    private String pass;

    public void setCredentials(String user, String pass) {
        this.user = user;
        this.pass = pass;
    }

    public void configure(HttpURLConnection connection) {

        byte[] encodedBytes = Base64.encode((user + ":" + pass).getBytes(), Base64.DEFAULT);

        // add custom HTTP header
        connection.addRequestProperty("Authorization", "Basic "+ new String(encodedBytes));
    }
}