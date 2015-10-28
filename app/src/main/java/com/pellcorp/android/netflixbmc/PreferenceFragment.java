package com.pellcorp.android.netflixbmc;

import android.os.Bundle;
import android.preference.EditTextPreference;
import android.preference.Preference;
import android.preference.Preference.OnPreferenceChangeListener;
import android.widget.Toast;

import com.pellcorp.android.netflixbmc.jsonrpc.JsonClient;
import com.pellcorp.android.netflixbmc.jsonrpc.JsonClientImpl;
import com.pellcorp.android.netflixbmc.jsonrpc.KodiNetflixChecker;
import com.pellcorp.android.netflixbmc.jsonrpc.KodiNetflixChecker.KodiNetflixCheckerStatus;

public class PreferenceFragment extends android.preference.PreferenceFragment {
	public PreferenceFragment() {
	}

	@Override
    public void onCreate(Bundle savedInstanceState) {
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
                        Toast.makeText(getActivity(), R.string.kodi_url_config_is_valid, Toast.LENGTH_SHORT).show();
                        return true;
                    } else if (status.equals(KodiNetflixCheckerStatus.MISSING_PLUGIN)) {
                        ActivityUtils.createErrorDialog(getActivity(), "No netflixbmc plugin");
                        return false;
                    } else {
                        ActivityUtils.createErrorDialog(getActivity(), "Kodi instance not accessible");
                        return false;
                    }
                } catch (Exception e) {
                    ActivityUtils.createErrorDialog(getActivity(), e.getMessage());
                    return false;
                }
            }
        });
	}
}
