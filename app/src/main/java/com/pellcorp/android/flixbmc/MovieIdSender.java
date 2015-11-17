package com.pellcorp.android.flixbmc;

import android.app.Activity;
import android.os.AsyncTask;
import android.widget.Toast;

import com.pellcorp.android.flixbmc.jsonrpc.JsonClient;
import com.pellcorp.android.flixbmc.jsonrpc.JsonClientResponse;
import com.pellcorp.android.flixbmc.web.NetflixUrl;
import static com.pellcorp.android.flixbmc.ActivityUtils.DialogType.OK_FINISH_NO_CANCEL;

import java.util.HashMap;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class MovieIdSender {
	private final Logger logger = LoggerFactory.getLogger(getClass());
	private final JsonClient client;
	private final Activity activity;

	public MovieIdSender(JsonClient client, final Activity activity) {
		this.client = client;
        this.activity = activity;
	}
	
	public void sendMovie(NetflixUrl url) {
        AsyncTask<NetflixUrl, Integer, JsonClientResponse> asyncTask = new AsyncTask<NetflixUrl, Integer, JsonClientResponse>() {
            @Override
            protected void onPreExecute() {
                super.onPreExecute();
            }

            @Override
            protected JsonClientResponse doInBackground(NetflixUrl ... params) {
                return doSendMovie(params[0]);
            }

            @Override
            protected void onPostExecute(JsonClientResponse result) {
                if (result.isSuccess()) {
                    Toast.makeText(activity, R.string.play_on_kodi_request_successful, Toast.LENGTH_LONG).show();
                    activity.finish();
                } else if (result.isError()) {
                    ActivityUtils.createErrorDialog(activity, R.string.kodi_instance_not_accessible, OK_FINISH_NO_CANCEL);
                }
                super.onPostExecute(result);
            }
        };

        asyncTask.execute(url);
    }

    private String getString(int id) {
        return activity.getString(id);
    }

    private JsonClientResponse doSendMovie(NetflixUrl netflixUrl) {
        // TODO - we could call the listVideos for 'title' url's, unfortunately
        // we have no way to know if a title is a 'movie' or a series, perhaps there is
        // something else in the originating page that we can get access to to make that decision
        String movieId = netflixUrl.getId();

		logger.info("movieId {}", movieId);
		
		Map<String, Object> params = new HashMap<String, Object>();
		Map<String, Object> itemParams = new HashMap<String, Object>();
		params.put("item", itemParams);
		itemParams.put("file", "plugin://plugin.video.netflixbmc/?mode=playVideo&url=" + movieId);
		JsonClientResponse response = client.send("Player.Open", params);
		return response;
	}
}
