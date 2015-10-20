package com.pellcorp.android.netflixbmc.jsonrpc;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import net.minidev.json.JSONArray;
import net.minidev.json.JSONObject;

public class KodiNetflixChecker {
	public enum KodiNetflixCheckerStatus {
		CONNECT_EXCEPTION, MISSING_PLUGIN, NORMAL
	}

	public static final String PLUGIN_PROGRAM_CHROME_LAUNCHER = "plugin.program.chrome.launcher";
	public static final String PLUGIN_VIDEO_NETFLIXBMC = "plugin.video.netflixbmc";

	private final JsonClient client;
	
	public KodiNetflixChecker(JsonClient client) {
		this.client = client;
	}
	
	public KodiNetflixCheckerStatus doCheck() {
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
}
