package com.pellcorp.android.flixbmc;

import android.app.Activity;
import android.app.Dialog;
import android.os.Bundle;
import android.preference.Preference;
import android.widget.Toast;

import com.pellcorp.android.flixbmc.jsonrpc.JsonClient;
import com.pellcorp.android.flixbmc.jsonrpc.JsonClientImpl;
import com.pellcorp.android.flixbmc.jsonrpc.KodiNetflixChecker;

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
    public void onBackPressed()
    {
        if(areSettingsValid() )
            super.onBackPressed();
    }

    private boolean areSettingsValid()
    {
        Preferences preferences = new Preferences(this);
        try {
            JsonClient jsonClient = new JsonClientImpl(preferences.getString(R.string.pref_host_url),
                    preferences.getString(R.string.pref_kodi_username),
                    preferences.getString(R.string.pref_kodi_password));
            KodiNetflixChecker checker = new KodiNetflixChecker(jsonClient);
            KodiNetflixChecker.KodiNetflixCheckerStatus status = checker.check();
            if (status.equals(KodiNetflixChecker.KodiNetflixCheckerStatus.NORMAL)) {
                Toast.makeText(preferenceFragment.getActivity(), R.string.kodi_url_config_is_valid, Toast.LENGTH_SHORT).show();
            } else if (status.equals(KodiNetflixChecker.KodiNetflixCheckerStatus.MISSING_PLUGIN)) {
                Dialog dialog = ActivityUtils.createErrorDialog(preferenceFragment.getActivity(),
                        preferences.getString(R.string.invalid_kodi_settings),
                        "No netflixbmc plugin", false);
                dialog.show();
                return false;
            } else {
                Dialog dialog = ActivityUtils.createErrorDialog(preferenceFragment.getActivity(),
                        preferences.getString(R.string.invalid_kodi_settings),
                        "Kodi instance not accessible", false);
                dialog.show();
                return false;
            }
        } catch (Exception e) {
            Dialog dialog = ActivityUtils.createErrorDialog(preferenceFragment.getActivity(),
                    preferences.getString(R.string.invalid_kodi_settings),
                    e.getMessage(), false);
            dialog.show();
            return false;
        }

        return true;
    }
}