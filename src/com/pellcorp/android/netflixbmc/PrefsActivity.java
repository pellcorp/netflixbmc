package com.pellcorp.android.netflixbmc;

import java.net.MalformedURLException;
import java.net.URL;

import android.app.AlertDialog;
import android.os.Bundle;
import android.preference.EditTextPreference;
import android.preference.Preference;
import android.preference.Preference.OnPreferenceChangeListener;
import android.preference.PreferenceActivity;

public class PrefsActivity extends PreferenceActivity {
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		addPreferencesFromResource(R.xml.settings);
		
		EditTextPreference urlPref = (EditTextPreference) 
				getPreferenceScreen().findPreference(getString(R.string.pref_host_url));
		
		urlPref.setOnPreferenceChangeListener(new OnPreferenceChangeListener() {
	        @Override
	        public boolean onPreferenceChange(Preference preference, Object newValue) {
	            String url = (String) newValue;
	            
	            try {
	            	new URL(url);
	            	return true;
	            } catch(MalformedURLException e) {
	                final AlertDialog.Builder builder = new AlertDialog.Builder(PrefsActivity.this);
	                builder.setTitle("Invalid URL");
	                builder.setMessage(e.getMessage());
	                builder.setPositiveButton(android.R.string.ok, null);
	                builder.show();
	                return false;
	            }
	        }
	    });
	}
}
