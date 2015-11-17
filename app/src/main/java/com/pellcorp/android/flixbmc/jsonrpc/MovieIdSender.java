package com.pellcorp.android.flixbmc.jsonrpc;

import android.app.Activity;
import android.app.Dialog;
import android.os.AsyncTask;
import android.widget.Toast;

import com.pellcorp.android.flixbmc.ActivityUtils;
import com.pellcorp.android.flixbmc.NetflixUrl;
import com.pellcorp.android.flixbmc.R;

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
	
	public JsonClientResponse sendMovie(NetflixUrl url) {
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
                    Toast.makeText(activity, R.string.successful_submission, Toast.LENGTH_SHORT).show();
                } else if (result.isError()) {
                    Dialog dialog = ActivityUtils.createErrorDialog(activity,
                            activity.getString(R.string.unexpected_error),
                            result.getErrorMessage(),
                            false);
                    dialog.show();
                }
                super.onPostExecute(result);
            }
        };

        try {
            return asyncTask.execute(url).get();
        } catch (Exception e) {
            logger.error("Failed to execute", e);
            return new JsonClientResponse(e);
        }
    }

    private JsonClientResponse doSendMovie(NetflixUrl netflixUrl) {
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
