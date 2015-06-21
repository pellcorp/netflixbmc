package com.pellcorp.android.netflixbmc;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;

public class Preferences {
	private final PreferenceProvider preferenceProvider;
	
	public Preferences(final PreferenceProvider preferenceProvider) {
		this.preferenceProvider = preferenceProvider;
	}
	
	public static Preferences getPreferences(Context ctx) {
		SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(ctx);
		return new Preferences(new PreferenceProviderImpl(ctx, sharedPreferences));
	}
	
	public boolean isConfigured() {
		return getUrl() != null;
	}
	
	public String getUrl() {
		String url = preferenceProvider.getString(R.string.pref_host_url);
		if (url.endsWith("/")) {
			url = url.substring(0, url.length() - 2);
		}
		return url + "/jsonrpc";
	}
}
