package com.pellcorp.android.flixbmc;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;

public class Preferences {
    private Context ctx;
    private SharedPreferences preferences;

    public Preferences(Context ctx) {
        this.ctx = ctx;
        this.preferences = PreferenceManager.getDefaultSharedPreferences(ctx);

    }

    public String getString(int resId) {
        String value = preferences.getString(ctx.getString(resId), null);
        if (value != null && value.length() == 0) {
            return null;
        } else {
            return value;
        }
    }

    public void registerOnPreferenceChangeListener(SharedPreferences.OnSharedPreferenceChangeListener preferenceChangeListener) {
        preferences.registerOnSharedPreferenceChangeListener(preferenceChangeListener);
    }
}
