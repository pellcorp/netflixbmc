package com.pellcorp.android.netflixbmc;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;

/**
 * Created by jason on 20/10/15.
 */
public class Preferences {
    private Context ctx;
    private SharedPreferences preferences;

    public Preferences(Context ctx) {
        this.ctx = ctx;
        this.preferences = PreferenceManager.getDefaultSharedPreferences(ctx);
    }

    public boolean isConfigured() {
        return getString(R.string.pref_host_url) != null
                && getString(R.string.pref_netflix_username) != null
                && getString(R.string.pref_netflix_password) != null;
    }

    public String getString(int resId) {
        String value = preferences.getString(ctx.getString(resId), null);
        if (value != null && value.length() == 0) {
            return null;
        } else {
            return value;
        }
    }
}
