package com.pellcorp.android.flixbmc;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.os.Bundle;

import com.pellcorp.android.flixbmc.jsonrpc.JsonClient;
import com.pellcorp.android.flixbmc.jsonrpc.JsonClientImpl;

import static com.pellcorp.android.flixbmc.KodiNetflixCheckerStatus.CONNECT_EXCEPTION;
import static com.pellcorp.android.flixbmc.KodiNetflixCheckerStatus.MISSING_PLUGIN;

public class PreferenceActivity extends Activity implements KodiNetflixCheckerListener {
    private PreferenceFragment preferenceFragment = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        preferenceFragment = new PreferenceFragment();

        getFragmentManager().beginTransaction().replace(android.R.id.content, preferenceFragment).commit();
    }

    @Override
    public void onBackPressed() {
        checkSettings();
        // note we are not executing super.onBackPress, because that will be executed in onPostExecute if appropriate
    }

    @Override
    public void onPostExecute(KodiNetflixCheckerStatus status) {
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
    }

    private void checkSettings() {
        Preferences preferences = new Preferences(this);
        ProgressDialog progressDialog = ActivityUtils.createProgressDialog(PreferenceActivity.this);

        KodiNetflixChecker checker = new KodiNetflixChecker(preferences, progressDialog);
        checker.doCheck(this);
    }

    private void showInvalidSettingsDialog(int title, int message) {
        showInvalidSettingsDialog(title, getString(message));
    }

    private void showInvalidSettingsDialog(int title, String message) {
        final AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setMessage(message);
        builder.setTitle(title);

        builder.setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
            }
        });

        builder.setNegativeButton(R.string.ignore, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
                PreferenceActivity.super.onBackPressed();
            }
        });

        AlertDialog dialog = builder.create();
        dialog.show();
    }
}