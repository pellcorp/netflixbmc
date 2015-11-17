package com.pellcorp.android.flixbmc;

import android.app.Activity;
import android.app.Dialog;
import android.content.Intent;
import android.os.Bundle;
import com.pellcorp.android.flixbmc.jsonrpc.JsonClient;
import com.pellcorp.android.flixbmc.jsonrpc.JsonClientImpl;
import com.pellcorp.android.flixbmc.jsonrpc.JsonClientResponse;
import com.pellcorp.android.flixbmc.jsonrpc.MovieIdSender;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class SendToKodiActivity extends Activity {
    private final Logger logger = LoggerFactory.getLogger(getClass());

    public static final String SEND_TO_KODI = "com.pellcorp.android.action.SEND_TO_KODI";
    public static final String NETFLIX_URL = "com.pellcorp.android.flixbmc.NETFLIX_URL";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        Intent intent = getIntent();
        String action = intent.getAction();

        if (Intent.ACTION_SEND.equals(action)) {
            handleNetflixShare(intent);
        } else if (SEND_TO_KODI.equals(action)) {
            handleSendToKodi(intent);
        } else {
            finish();
        }
    }

    private void handleNetflixShare(Intent intent) {
        String sharedText = intent.getStringExtra(Intent.EXTRA_TEXT);
        logger.debug("Netflix Shared Text: {}", sharedText);

        if (sharedText != null) {
            sendToKodi(sharedText);
            finish();
        }
    }

    private void handleSendToKodi(Intent intent) {
        String url = intent.getStringExtra(NETFLIX_URL);
        logger.debug("Send To Netflix URL: {}", url);
        sendToKodi(url);
    }

    private void sendToKodi(String netflixUrl) {
        Preferences preferences = new Preferences(this);

        String url = preferences.getString(R.string.pref_host_url);
        String kodi_username = preferences.getString(R.string.pref_kodi_username);
        String kodi_password = preferences.getString(R.string.pref_kodi_password);

        if (url != null) {
            JsonClient jsonClient = new JsonClientImpl(url, kodi_username, kodi_password);

            MovieIdSender sender = new MovieIdSender(jsonClient, this);
            JsonClientResponse response = sender.sendMovie(netflixUrl);

            if(!response.isSuccess() ) {
                Dialog dialog = ActivityUtils.createSettingsMissingDialog(this,
                        getString(R.string.invalid_kodi_settings), true);
                dialog.show();
            }

            finish();
        } else {
            Dialog dialog = ActivityUtils.createSettingsMissingDialog(this,
                    getString(R.string.missing_kodi_connection_settings), true);
            dialog.show();
        }
    }
}
