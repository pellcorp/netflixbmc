package com.pellcorp.android.netflixbmc;

public interface PreferenceProvider {
	String getString(int resId);
	boolean getBoolean(int resId);
	int getInteger(int resId, int defaultValue);
}
