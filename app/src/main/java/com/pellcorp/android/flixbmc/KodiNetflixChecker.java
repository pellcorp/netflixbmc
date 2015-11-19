package com.pellcorp.android.flixbmc;

import android.os.AsyncTask;

import com.pellcorp.android.flixbmc.jsonrpc.JsonClient;
import com.pellcorp.android.flixbmc.jsonrpc.JsonClientImpl;
import com.pellcorp.android.flixbmc.jsonrpc.JsonClientResponse;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import net.minidev.json.JSONArray;
import net.minidev.json.JSONObject;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class KodiNetflixChecker {
    private final Logger logger = LoggerFactory.getLogger(getClass().getName());

	public static final String PLUGIN_VIDEO_NETFLIXBMC = "plugin.video.netflixbmc";

	private final JsonClient client;
    private final ProgressSpinner progressSpinner;

	public KodiNetflixChecker(Preferences preferences, final ProgressSpinner progressSpinner) {
        this.client = new JsonClientImpl(
                preferences.getString(R.string.pref_host_url),
                preferences.getString(R.string.pref_kodi_username),
                preferences.getString(R.string.pref_kodi_password));

        this.progressSpinner = progressSpinner;
	}

    public void doCheck(final KodiNetflixCheckerListener listener) {
        AsyncTask<Void, Integer, KodiNetflixCheckerStatus> asyncTask = new AsyncTask<Void, Integer, KodiNetflixCheckerStatus>() {
            @Override
            protected void onPreExecute() {
                progressSpinner.showSpinner();
            }

            @Override
            protected KodiNetflixCheckerStatus doInBackground(Void ... params) {
				return doCheck();
            }

            @Override
            protected void onPostExecute(KodiNetflixCheckerStatus result) {
                progressSpinner.hideSpinner();

                listener.onPostExecute(result);
                super.onPostExecute(result);
            }
        };

        asyncTask.execute();
    }

	private KodiNetflixCheckerStatus doCheck() {
		try {
			Map<String, Object> params = new HashMap<String, Object>();
			params.put("type", "xbmc.python.pluginsource");
			JsonClientResponse response = client.send("Addons.GetAddons", params);
			if (response.isSuccess()) {
				JSONObject obj = response.getResponse();
				JSONObject result = (JSONObject) obj.get("result");
				JSONArray addons = (JSONArray) result.get("addons");

				List<String> addonList = new ArrayList<String>(addons.size());
				for (int i = 0; i < addons.size(); i++) {
					JSONObject addon = (JSONObject) addons.get(i);
					String addonId = (String) addon.get("addonid");
					addonList.add(addonId);
				}

				if (addonList.contains(PLUGIN_VIDEO_NETFLIXBMC)) {
					return KodiNetflixCheckerStatus.NORMAL;
				} else {
					return KodiNetflixCheckerStatus.MISSING_PLUGIN;
				}
			} else {
				return KodiNetflixCheckerStatus.CONNECT_EXCEPTION;
			}
		}
		catch (Exception e) {
			logger.error("Failed to execute", e);
			return KodiNetflixCheckerStatus.CONNECT_EXCEPTION;
		}
	}
}
