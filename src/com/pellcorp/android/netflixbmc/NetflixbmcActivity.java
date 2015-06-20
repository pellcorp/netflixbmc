package com.pellcorp.android.netflixbmc;

import java.io.PrintWriter;
import java.io.StringWriter;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;

import com.pellcorp.android.netflixbmc.jsonrpc.JsonClient;
import com.pellcorp.android.netflixbmc.jsonrpc.JsonClientImpl;
import com.pellcorp.android.netflixbmc.jsonrpc.MovieIdSender;

public class NetflixbmcActivity extends Activity {
	private final Logger logger = LoggerFactory.getLogger(getClass().getName());

	private Preferences preferences;
	
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		preferences = Preferences.getPreferences(this);
		
		logger.info("Starting onCreate");
		logger.info("Starting onStart");

		if (!preferences.isConfigured()) {
			Dialog dialog = createSettingsMissingDialog(getString(R.string.missing_connection_details));
			dialog.show();
		} else {
			final Intent intent = getIntent();
			String url = intent.getDataString();

			JsonClient jsonClient = new JsonClientImpl(preferences.getUrl());
			
			try {
				SendToXbmc task = new SendToXbmc(jsonClient);
				Boolean result = task.execute(url).get();
				if (result) {
					finish();
				} else {
					Dialog dialog = createErrorDialog("Submission failed");
					dialog.show();
				}
			} catch (Exception e) {
				StringWriter swriter = new StringWriter();
				PrintWriter writer = new PrintWriter(swriter);
				e.printStackTrace(writer);
				Dialog dialog = createErrorDialog(swriter.toString());
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
			startActivity(new Intent(this, PrefsActivity.class));
			return true;
		}
		return false;
	}

	private AlertDialog createErrorDialog(String message) {
		AlertDialog.Builder builder = new AlertDialog.Builder(this);
		builder.setMessage(message)
				.setCancelable(false)
				.setNeutralButton(android.R.string.ok,
						new DialogInterface.OnClickListener() {
							public void onClick(DialogInterface dialog, int id) {
								dialog.cancel();
							}
						});

		return builder.create();
	}

	private AlertDialog createSettingsMissingDialog(String message) {
		AlertDialog.Builder builder = new AlertDialog.Builder(this);

		builder.setMessage(message)
				.setCancelable(true)
				.setPositiveButton(R.string.settings_label,
						new DialogInterface.OnClickListener() {
							public void onClick(DialogInterface dialog, int id) {
								startActivity(new Intent(
										NetflixbmcActivity.this,
										PrefsActivity.class));
							}
						});
		return builder.create();
	}
	
	private class SendToXbmc extends AsyncTask<String, Integer, Boolean> {
		private MovieIdSender sender;
		
		public SendToXbmc(JsonClient jsonClient) {
			sender = new MovieIdSender(jsonClient);
		}
		
		@Override
		protected Boolean doInBackground(String... params) {
			return sender.sendMovie(params[0]);
		}
	}
}
