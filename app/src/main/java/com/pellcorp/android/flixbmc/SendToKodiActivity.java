package com.pellcorp.android.flixbmc;

import android.app.Activity;
import android.app.Dialog;
import android.content.Intent;
import android.os.Bundle;
import com.pellcorp.android.flixbmc.jsonrpc.JsonClient;
import com.pellcorp.android.flixbmc.jsonrpc.JsonClientImpl;
import com.pellcorp.android.flixbmc.web.NetflixUrl;
import static com.pellcorp.android.flixbmc.ActivityUtils.OnCloseType.FINISH;

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

        if (sharedText != null) {
            logger.debug("Netflix Shared Text: {}", sharedText);
            NetflixUrl netflixUrl = new NetflixUrl(sharedText);
            sendToKodi(netflixUrl);
            finish();
        }
    }

    private void handleSendToKodi(Intent intent) {
        String sharedText = intent.getStringExtra(NETFLIX_URL);

        if (sharedText != null) {
            NetflixUrl netflixUrl = new NetflixUrl(sharedText);
            sendToKodi(netflixUrl);
        }
    }

    private void sendToKodi(NetflixUrl netflixUrl) {
        Preferences preferences = new Preferences(this);

        String url = preferences.getString(R.string.pref_host_url);
        String kodi_username = preferences.getString(R.string.pref_kodi_username);
        String kodi_password = preferences.getString(R.string.pref_kodi_password);

        if (netflixUrl.isNetflixUrl()) {
            logger.debug("Send To Netflix URL: {}", url);

            JsonClient jsonClient = new JsonClientImpl(url, kodi_username, kodi_password);

            // the send movie call will 'finish' this activity
            MovieIdSender sender = new MovieIdSender(jsonClient, this);
            sender.sendMovie(netflixUrl);
        } else {
            ActivityUtils.createErrorDialog(this, R.string.netflix_title_url_not_supported, FINISH);
        }
    }
}
