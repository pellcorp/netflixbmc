package com.pellcorp.android.flixbmc.jsonrpc;

import android.os.AsyncTask;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import net.minidev.json.JSONArray;
import net.minidev.json.JSONObject;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static com.pellcorp.android.flixbmc.jsonrpc.KodiNetflixCheckerStatus.CONNECT_EXCEPTION;
import static com.pellcorp.android.flixbmc.jsonrpc.KodiNetflixCheckerStatus.NORMAL;
import static com.pellcorp.android.flixbmc.jsonrpc.KodiNetflixCheckerStatus.MISSING_PLUGIN;

public class KodiNetflixChecker {
    private final Logger logger = LoggerFactory.getLogger(getClass().getName());

	public static final String PLUGIN_VIDEO_NETFLIXBMC = "plugin.video.netflixbmc";

	private final JsonClient client;
	
	public KodiNetflixChecker(JsonClient client) {
		this.client = client;
	}

    public KodiNetflixCheckerStatus check() {
        AsyncTask<Void, Integer, KodiNetflixCheckerStatus> asyncTask = new AsyncTask<Void, Integer, KodiNetflixCheckerStatus>() {
            @Override
            protected KodiNetflixCheckerStatus doInBackground(Void ... params) {
				return doCheck();
            }
        };

		try {
			return asyncTask.execute().get();
		} catch (Exception e) {
			logger.error("Failed to execute", e);
			return CONNECT_EXCEPTION;
		}
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
					return NORMAL;
				} else {
					return MISSING_PLUGIN;
				}
			} else {
				return CONNECT_EXCEPTION;
			}
		}
		catch (Exception e) {
			logger.error("Failed to execute", e);
			return CONNECT_EXCEPTION;
		}
	}
}
