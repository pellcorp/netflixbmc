package com.pellcorp.android.flixbmc;

import android.app.Dialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.EditTextPreference;
import android.preference.Preference;
import android.preference.PreferenceGroup;
import android.text.method.PasswordTransformationMethod;
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

            if(listPreference.getEditText().getTransformationMethod() != PasswordTransformationMethod.getInstance() )
                listPreference.setSummary(listPreference.getText());
        }
    }

    @Override
    public void onDestroy()
    {
        try {
            JsonClient jsonClient = new JsonClientImpl(getString(R.string.pref_host_url), getString(R.string.pref_kodi_username), getString(R.string.pref_kodi_password));
            KodiNetflixChecker checker = new KodiNetflixChecker(jsonClient);
            KodiNetflixChecker.KodiNetflixCheckerStatus status = checker.check();
            if (status.equals(KodiNetflixChecker.KodiNetflixCheckerStatus.NORMAL)) {
                Toast.makeText(getActivity(), R.string.kodi_url_config_is_valid, Toast.LENGTH_SHORT).show();
            } else if (status.equals(KodiNetflixChecker.KodiNetflixCheckerStatus.MISSING_PLUGIN)) {
                Dialog dialog = ActivityUtils.createErrorDialog(getActivity(),
                        getString(R.string.invalid_kodi_settings),
                        "No netflixbmc plugin", false);
                dialog.show();
            } else {
                Dialog dialog = ActivityUtils.createErrorDialog(getActivity(),
                        getString(R.string.invalid_kodi_settings),
                        "Kodi instance not accessible", false);
                dialog.show();
            }
        } catch (Exception e) {
            Dialog dialog = ActivityUtils.createErrorDialog(getActivity(),
                    getString(R.string.invalid_kodi_settings),
                    e.getMessage(), false);
            dialog.show();
        }

        super.onDestroy();
    }
}
