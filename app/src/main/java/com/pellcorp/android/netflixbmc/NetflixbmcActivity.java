package com.pellcorp.android.netflixbmc;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import android.app.Activity;
import android.app.Dialog;
import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.widget.Toast;

import com.pellcorp.android.netflixbmc.jsonrpc.JsonClient;
import com.pellcorp.android.netflixbmc.jsonrpc.JsonClientImpl;
import com.pellcorp.android.netflixbmc.jsonrpc.JsonClientResponse;
import com.pellcorp.android.netflixbmc.jsonrpc.MovieIdSender;

public class NetflixbmcActivity extends Activity {
	private final Logger logger = LoggerFactory.getLogger(getClass().getName());

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

        logger.info("Starting onCreate");

        Preferences preferences = new Preferences(this);
        if (!preferences.isConfigured()) {
			Dialog dialog = ActivityUtils.createSettingsMissingDialog(this, getString(R.string.missing_connection_details));
			dialog.show();
		} else {
			try {
                String url = preferences.getString(R.string.pref_host_url);
				JsonClient jsonClient = new JsonClientImpl(url);

                MovieIdSender task = new MovieIdSender(jsonClient);
				JsonClientResponse result = task.sendMovie(url);
				
				if (result.isSuccess()) {
					Toast.makeText(this, R.string.successful_submission, Toast.LENGTH_SHORT).show();
					finish();
				} else if (result.isError()) {
					Dialog dialog = ActivityUtils.createErrorDialog(this, result.getErrorMessage());
					dialog.show();
				}
			} catch (Exception e) {
                logger.error("failed to send", e);
				Dialog dialog = ActivityUtils.createErrorDialog(this, e.getMessage());
				dialog.show();
			}
		}
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		super.onCreateOptionsMenu(menu);
		MenuInflater inflater = getMenuInflater();
		inflater.inflate(R.menu.menu, menu);
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
		case R.id.settings:
			startActivity(new Intent(this, PreferenceActivity.class));
			return true;
		}
		return false;
	}
}
