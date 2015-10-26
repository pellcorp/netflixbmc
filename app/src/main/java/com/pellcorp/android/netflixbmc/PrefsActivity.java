package com.pellcorp.android.netflixbmc;

import java.net.MalformedURLException;
import java.net.URL;

import android.app.AlertDialog;
import android.app.Dialog;
import android.os.AsyncTask;
import android.os.Bundle;
import android.preference.EditTextPreference;
import android.preference.Preference;
import android.preference.Preference.OnPreferenceChangeListener;
import android.preference.PreferenceActivity;
import android.widget.Toast;

import com.pellcorp.android.netflixbmc.jsonrpc.JsonClient;
import com.pellcorp.android.netflixbmc.jsonrpc.JsonClientImpl;
import com.pellcorp.android.netflixbmc.jsonrpc.KodiNetflixChecker;
import com.pellcorp.android.netflixbmc.jsonrpc.KodiNetflixChecker.KodiNetflixCheckerStatus;

public class PrefsActivity extends PreferenceActivity {
	public PrefsActivity() {
	}

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		addPreferencesFromResource(R.xml.settings);
		
		EditTextPreference urlPref = (EditTextPreference) getPreferenceScreen().findPreference(getString(R.string.pref_host_url));

		urlPref.setOnPreferenceChangeListener(new OnPreferenceChangeListener() {
            @Override
            public boolean onPreferenceChange(Preference preference, Object newValue) {
                String urlString = (String) newValue;

                try {
                    JsonClient jsonClient = new JsonClientImpl(urlString);
                    KodiNetflixChecker checker = new KodiNetflixChecker(jsonClient);
                    KodiNetflixCheckerStatus status = checker.check();
                    if (status.equals(KodiNetflixCheckerStatus.NORMAL)) {
                        Toast.makeText(PrefsActivity.this, R.string.kodi_url_config_is_valid, Toast.LENGTH_SHORT).show();
                        return true;
                    } else if (status.equals(KodiNetflixCheckerStatus.MISSING_PLUGIN)) {
                        ActivityUtils.createErrorDialog(PrefsActivity.this, "No netflixbmc plugin");
                        return false;
                    } else {
                        ActivityUtils.createErrorDialog(PrefsActivity.this, "Kodi instance not accessible");
                        return false;
                    }
                } catch (Exception e) {
                    ActivityUtils.createErrorDialog(PrefsActivity.this, e.getMessage());
                    return false;
                }
            }
        });
	}
}
