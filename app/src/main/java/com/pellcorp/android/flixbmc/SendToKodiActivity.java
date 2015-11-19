package com.pellcorp.android.flixbmc;

import android.app.Activity;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.widget.Toast;

import com.pellcorp.android.flixbmc.jsonrpc.JsonClient;
import com.pellcorp.android.flixbmc.jsonrpc.JsonClientImpl;
import com.pellcorp.android.flixbmc.jsonrpc.JsonClientResponse;
import com.pellcorp.android.flixbmc.web.NetflixUrl;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashMap;
import java.util.Map;

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
        } else {
            finish();
        }
    }

    private void handleSendToKodi(Intent intent) {
        String sharedText = intent.getStringExtra(NETFLIX_URL);

        if (sharedText != null) {
            NetflixUrl netflixUrl = new NetflixUrl(sharedText);
            sendToKodi(netflixUrl);
        } else {
            finish();
        }
    }

    public void sendToKodi(final NetflixUrl netflixUrl) {
        if (netflixUrl.isNetflixUrl()) {
            AsyncTask<Void, Integer, JsonClientResponse> asyncTask = new AsyncTask<Void, Integer, JsonClientResponse>() {
                @Override
                protected void onPreExecute() {
                    super.onPreExecute();

                    doPreExecute();
                }

                @Override
                protected JsonClientResponse doInBackground(Void ... params) {
                    return doSendMovie(netflixUrl);
                }

                @Override
                protected void onPostExecute(JsonClientResponse result) {
                    super.onPostExecute(result);

                    doPostExecute(result);
                }
            };

            asyncTask.execute();

        } else {
            Toast.makeText(this, R.string.netflix_share_not_supported, Toast.LENGTH_SHORT).show();
            finish();
        }
    }

    private void doPreExecute() {
        Toast.makeText(this, R.string.connecting_to_kodi, Toast.LENGTH_LONG).show();
    }

    private void doPostExecute(JsonClientResponse result) {
        if (result.isSuccess()) {
            Toast.makeText(this, R.string.play_on_kodi_request_successful, Toast.LENGTH_LONG).show();
            finish();
        } else if (result.isError()) {
            Toast.makeText(this, R.string.kodi_instance_not_accessible, Toast.LENGTH_LONG).show();
            finish();
        }
    }

    private JsonClientResponse doSendMovie(NetflixUrl netflixUrl) {
        Preferences preferences = new Preferences(this);

        String url = preferences.getString(R.string.pref_host_url);
        String kodi_username = preferences.getString(R.string.pref_kodi_username);
        String kodi_password = preferences.getString(R.string.pref_kodi_password);

        logger.debug("Send To Netflix URL: {}", url);

        JsonClient jsonClient = new JsonClientImpl(url, kodi_username, kodi_password);

        // TODO - we could call the listVideos for 'title' url's, unfortunately
        // we have no way to know if a title is a 'movie' or a series, perhaps there is
        // something else in the originating page that we can get access to to make that decision
        String movieId = netflixUrl.getId();

        logger.info("movieId {}", movieId);

        Map<String, Object> params = new HashMap<String, Object>();
        Map<String, Object> itemParams = new HashMap<String, Object>();
        params.put("item", itemParams);
        itemParams.put("file", "plugin://plugin.video.netflixbmc/?mode=playVideo&url=" + movieId);
        JsonClientResponse response = jsonClient.send("Player.Open", params);
        return response;
    }
}
