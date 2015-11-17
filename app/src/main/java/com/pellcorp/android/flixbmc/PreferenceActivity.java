package com.pellcorp.android.flixbmc;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.os.Bundle;

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
        checkSettings();
    }

    private void checkSettings() {
        Preferences preferences = new Preferences(this);
        try {
            JsonClient jsonClient = new JsonClientImpl(
                    preferences.getString(R.string.pref_host_url),
                    preferences.getString(R.string.pref_kodi_username),
                    preferences.getString(R.string.pref_kodi_password));

            KodiNetflixChecker checker = new KodiNetflixChecker(jsonClient);
            KodiNetflixCheckerStatus status = checker.check();

            if (status.equals(MISSING_PLUGIN)) {
                showInvalidSettingsDialog(
                        R.string.invalid_kodi_settings,
                        R.string.netflixbmc_plugin_not_installed);
            } else if (status.equals(CONNECT_EXCEPTION)) {
                showInvalidSettingsDialog(
                        R.string.invalid_kodi_settings,
                        R.string.kodi_instance_not_accessible);
            } else { // NORMAL
                super.onBackPressed();
            }
        } catch (Exception e) {
            // TODO - figure out a way to not display the exception!
            showInvalidSettingsDialog(
                    R.string.invalid_kodi_settings,
                    e.getMessage());
        }
    }

    private void showInvalidSettingsDialog(int title, int message) {
        showInvalidSettingsDialog(title, getString(message));
    }

    private void showInvalidSettingsDialog(int title, String message) {
        final AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setMessage(message);
        builder.setTitle(title);

        builder.setPositiveButton(android.R.string.ok, null);

        builder.setNegativeButton(R.string.ignore, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int which) {
                PreferenceActivity.super.onBackPressed();
            }
        });

        AlertDialog dialog = builder.create();
        dialog.show();
    }
}