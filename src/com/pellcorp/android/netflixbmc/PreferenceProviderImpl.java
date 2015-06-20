package com.pellcorp.android.netflixbmc;

import android.content.Context;
import android.content.SharedPreferences;

public class PreferenceProviderImpl implements PreferenceProvider {
	private final Context ctx;
	private final SharedPreferences sharedPreferences;
	
	public PreferenceProviderImpl(Context ctx, SharedPreferences sharedPreferences) {
		this.ctx = ctx;
		this.sharedPreferences = sharedPreferences;
	}
	
	@Override
	public String getString(int resId) {
		String value = sharedPreferences.getString(ctx.getString(resId), null);
		if (value != null && value.length() == 0) {
			return null;
		} else {
			return value;
		}
	}
	
	@Override
	public boolean getBoolean(int resId) {
		return sharedPreferences.getBoolean(ctx.getString(resId), false);
	}
	
	public int getInteger(int key, int defaultValue) {
		String value = getString(key);
		
		try {
			// FIXME - why are the preferences not loaded as numeric?!!
			return Integer.valueOf(value);
			//return sharedPreferences.getInt(key.getKey(), defaultValue);
		} catch (Exception e) {
			//Log.e(TAG, "Failed to load integer! " + key.getKey() 
			//		+ " = [" + value + "]", e);
			return defaultValue;
		}
	}
}
