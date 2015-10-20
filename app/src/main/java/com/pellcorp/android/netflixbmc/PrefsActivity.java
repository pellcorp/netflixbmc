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
import com.pellcorp.android.netflixbmc.jsonrpc.JsonClientResponse;
import com.pellcorp.android.netflixbmc.jsonrpc.JsonClientUtils;
import com.pellcorp.android.netflixbmc.jsonrpc.KodiNetflixChecker;
import com.pellcorp.android.netflixbmc.jsonrpc.KodiNetflixChecker.KodiNetflixCheckerStatus;
import com.pellcorp.android.netflixbmc.jsonrpc.MovieIdSender;

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
					RetrievePluginsFromXbmc task = new RetrievePluginsFromXbmc(jsonClient);
                    KodiNetflixCheckerStatus status = task.execute().get();
					if (status.equals(KodiNetflixCheckerStatus.NORMAL)) {
                        Toast.makeText(PrefsActivity.this, R.string.kodi_url_config_is_valid, Toast.LENGTH_SHORT).show();
						return true;
					} else if (status.equals(KodiNetflixCheckerStatus.MISSING_PLUGIN)) {
						showError("Invalid Kodi instance", "No netflixbmc plugin");
						return false;
					} else {
						showError("Invalid Kodi instance", "Kodi instance not accessible");
						return false;
					}
                } catch (Exception e) {
                    showError("Something unexpected happened", e.getMessage());
                    return false;
                }
	        }
	    });
	}

	private void showError(String error, String details) {
		final AlertDialog.Builder builder = new AlertDialog.Builder(PrefsActivity.this);
		builder.setTitle(error);
		builder.setMessage(details);
		builder.setPositiveButton(android.R.string.ok, null);
		builder.show();
	}

	private class RetrievePluginsFromXbmc extends AsyncTask<Void, Integer, KodiNetflixCheckerStatus> {
		private final KodiNetflixChecker checker;

		public RetrievePluginsFromXbmc(JsonClient jsonClient) {
			this.checker = new KodiNetflixChecker(jsonClient);
		}

		@Override
		protected KodiNetflixCheckerStatus doInBackground(Void... params) {
			return checker.doCheck();
		}
	}
}
