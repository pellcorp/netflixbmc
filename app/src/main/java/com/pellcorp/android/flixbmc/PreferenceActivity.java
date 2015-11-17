package com.pellcorp.android.flixbmc;

import android.app.Activity;
import android.app.Dialog;
import android.os.Bundle;
import android.widget.Toast;

import com.pellcorp.android.flixbmc.jsonrpc.JsonClient;
import com.pellcorp.android.flixbmc.jsonrpc.JsonClientImpl;
import com.pellcorp.android.flixbmc.jsonrpc.KodiNetflixChecker;
import com.pellcorp.android.flixbmc.jsonrpc.KodiNetflixCheckerStatus;

import static com.pellcorp.android.flixbmc.jsonrpc.KodiNetflixCheckerStatus.CONNECT_EXCEPTION;
import static com.pellcorp.android.flixbmc.jsonrpc.KodiNetflixCheckerStatus.MISSING_PLUGIN;

public class PreferenceActivity extends Activity {
    PreferenceFragment preferenceFragment = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        preferenceFragment = new PreferenceFragment();

        getFragmentManager().beginTransaction().replace(
                android.R.id.content,
                preferenceFragment)
                .commit();
    }

    @Override
    public void onBackPressed() {
        if(areSettingsValid()) {
            super.onBackPressed();
        }
    }

    private boolean areSettingsValid() {
        Preferences preferences = new Preferences(this);
        try {
            JsonClient jsonClient = new JsonClientImpl(
                    preferences.getString(R.string.pref_host_url),
                    preferences.getString(R.string.pref_kodi_username),
                    preferences.getString(R.string.pref_kodi_password));

            KodiNetflixChecker checker = new KodiNetflixChecker(jsonClient);
            KodiNetflixCheckerStatus status = checker.check();

            if (status.equals(MISSING_PLUGIN)) {
                Dialog dialog = ActivityUtils.createErrorDialog(
                        this,
                        getString(R.string.invalid_kodi_settings),
                        getString(R.string.netflixbmc_plugin_not_installed), false);
                dialog.show();
                return false;
            } else if (status.equals(CONNECT_EXCEPTION)) {
                Dialog dialog = ActivityUtils.createErrorDialog(
                        this,
                        getString(R.string.invalid_kodi_settings),
                        getString(R.string.kodi_instance_not_accessible),
                        false);
                dialog.show();
                return false;
            } else { // NORMAL
                return true;
            }
        } catch (Exception e) {
            Dialog dialog = ActivityUtils.createErrorDialog(
                    this,
                    getString(R.string.invalid_kodi_settings),
                    e.getMessage(), false);
            dialog.show();
            return false;
        }
    }
}