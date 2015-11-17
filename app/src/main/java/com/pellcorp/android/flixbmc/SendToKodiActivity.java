package com.pellcorp.android.flixbmc;

import android.app.Activity;
import android.app.Dialog;
import android.content.Intent;
import android.os.Bundle;
import com.pellcorp.android.flixbmc.jsonrpc.JsonClient;
import com.pellcorp.android.flixbmc.jsonrpc.JsonClientImpl;
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
            String url = getNetflixWatchUrl(sharedText);
            if (url != null) {
                sendToKodi(url);
                finish();
            } else {
                Dialog dialog = ActivityUtils.createErrorDialog(
                        this,
                        getString(R.string.url_not_supported),
                        getString(R.string.netflix_title_url_not_supported),
                        true);
                dialog.show();
            }
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
        if (url != null) {
            JsonClient jsonClient = new JsonClientImpl(url);

            MovieIdSender sender = new MovieIdSender(jsonClient, this);
            sender.sendMovie(netflixUrl);
            finish();
        } else {
            Dialog dialog = ActivityUtils.createSettingsMissingDialog(this,
                    getString(R.string.missing_settings), true);
            dialog.show();
        }
    }

    // netflix does not share 'watch' url,

    private String getNetflixWatchUrl(String url) {
        int indexOf = url.indexOf("www.netflix.com/title/");
        if (indexOf != -1) {
            url = url.substring(indexOf);
            int indexOfQuestion = url.indexOf("?");
            if (indexOfQuestion != -1) {
                url = url.substring(0, indexOfQuestion);
            }

            // TODO - we have no way to tell if this is a tv series or a movie,
            // but the SendToKodiActivity expects a /watch url, so for now this will
            // have to do
            url = "https://" + url.replace("/title/", "/watch/");

            logger.debug("Netflix URL: {}", url);
            return url;
        } else {
            return null;
        }
    }
}
