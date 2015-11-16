package com.pellcorp.android.flixbmc;

import android.app.Dialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.EditTextPreference;
import android.preference.Preference;
import android.preference.Preference.OnPreferenceChangeListener;
import android.preference.PreferenceGroup;
import android.widget.Toast;

import com.pellcorp.android.flixbmc.jsonrpc.KodiNetflixChecker;
import com.pellcorp.android.flixbmc.jsonrpc.JsonClient;
import com.pellcorp.android.flixbmc.jsonrpc.JsonClientImpl;

public class PreferenceFragment extends android.preference.PreferenceFragment implements SharedPreferences.OnSharedPreferenceChangeListener {
	public PreferenceFragment() {
	}

	@Override
    public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		addPreferencesFromResource(R.xml.settings);
        getPreferenceScreen().getSharedPreferences().registerOnSharedPreferenceChangeListener(this);
        initSummary(getPreferenceScreen());

        EditTextPreference urlPref = (EditTextPreference) findPreference(getString(com.pellcorp.android.flixbmc.R.string.pref_host_url));

		urlPref.setOnPreferenceChangeListener(new OnPreferenceChangeListener() {
            @Override
            public boolean onPreferenceChange(Preference preference, Object newValue) {
                String urlString = (String) newValue;
                try {
                    JsonClient jsonClient = new JsonClientImpl(urlString);
                    KodiNetflixChecker checker = new KodiNetflixChecker(jsonClient);
                    KodiNetflixChecker.KodiNetflixCheckerStatus status = checker.check();
                    if (status.equals(KodiNetflixChecker.KodiNetflixCheckerStatus.NORMAL)) {
                        Toast.makeText(getActivity(), R.string.kodi_url_config_is_valid, Toast.LENGTH_SHORT).show();
                        preference.setSummary(urlString);
                        return true;
                    } else if (status.equals(KodiNetflixChecker.KodiNetflixCheckerStatus.MISSING_PLUGIN)) {
                        Dialog dialog = ActivityUtils.createErrorDialog(getActivity(), "No netflixbmc plugin", false);
                        dialog.show();
                        return false;
                    } else {
                        Dialog dialog = ActivityUtils.createErrorDialog(getActivity(), "Kodi instance not accessible", false);
                        dialog.show();
                        return false;
                    }
                } catch (Exception e) {
                    Dialog dialog = ActivityUtils.createErrorDialog(getActivity(), e.getMessage(), false);
                    dialog.show();
                    return false;
                }
            }
        });
	}

    @Override
    public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {
        updatePreference(findPreference(key));
    }

    private void initSummary(Preference p) {
        if (p instanceof PreferenceGroup) {
            PreferenceGroup pGrp = (PreferenceGroup) p;
            for (int i = 0; i < pGrp.getPreferenceCount(); i++) {
                initSummary(pGrp.getPreference(i));
            }
        } else {
            updatePreference(p);
        }
    }

    private void updatePreference(Preference preference) {
        if (preference instanceof EditTextPreference) {
            EditTextPreference listPreference = (EditTextPreference) preference;
            listPreference.setSummary(listPreference.getText());
        }
    }
}
