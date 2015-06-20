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
		return getUsername() != null && getPassword() != null && getHost() != null;
	}
	
	public String getHost() {
		return preferenceProvider.getString(R.string.pref_host_url);
	}
	
	public String getUsername() {
		return preferenceProvider.getString(R.string.pref_username);
	}
	
	public String getPassword() {
		return preferenceProvider.getString(R.string.pref_password);
	}
}
