package com.pellcorp.android.flixbmc.jsonrpc;

import android.app.Activity;
import android.app.Dialog;
import android.os.AsyncTask;
import android.widget.Toast;

import com.pellcorp.android.flixbmc.ActivityUtils;
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
	
	//http://www.netflix.com/watch/70259443?trackId=13462050&tctx=1%2C0%2C48d00020-b7c9-46ea-ae58-219011a2ed29-16193513
	public JsonClientResponse sendMovie(String url) {
        AsyncTask<String, Integer, JsonClientResponse> asyncTask = new AsyncTask<String, Integer, JsonClientResponse>() {
            @Override
            protected void onPreExecute() {
                super.onPreExecute();
            }

            @Override
            protected JsonClientResponse doInBackground(String ... params) {
                return doSendMovie(params[0]);
            }

            @Override
            protected void onPostExecute(JsonClientResponse result) {
                if (result.isSuccess()) {
                    Toast.makeText(activity, R.string.successful_submission, Toast.LENGTH_SHORT).show();
                } else if (result.isError()) {
                    Dialog dialog = ActivityUtils.createErrorDialog(activity, result.getErrorMessage(), false);
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

    private JsonClientResponse doSendMovie(String url) {
		logger.info("URL {}", url);
		String movieId = getMovieId(url);
		
		logger.info("movieId {}", movieId);
		
		Map<String, Object> params = new HashMap<String, Object>();
		Map<String, Object> itemParams = new HashMap<String, Object>();
		params.put("item", itemParams);
		itemParams.put("file", "plugin://plugin.video.netflixbmc/?mode=playVideo&url=" + movieId);
		JsonClientResponse response = client.send("Player.Open", params);
		return response;
	}
	
	private String getMovieId(String url) {
		int indexOf = url.indexOf("netflix.com/watch/");
		if (indexOf != -1) {
			url = url.substring(indexOf + "netflix.com/watch/".length());
			int indexOfQuestion = url.indexOf("?");
			if (indexOfQuestion != -1) {
				url = url.substring(0, indexOfQuestion);
			}
		}
		return url;
	}
}
