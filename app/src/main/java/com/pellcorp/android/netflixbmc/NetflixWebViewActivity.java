package com.pellcorp.android.netflixbmc;

import android.app.Activity;
import android.app.Dialog;
import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.webkit.WebView;
import android.app.AlertDialog;
import android.content.DialogInterface;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class NetflixWebViewActivity extends Activity {
    private final Logger logger = LoggerFactory.getLogger(getClass().getName());

	private WebView webView;

	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.webview);

		webView = (WebView) findViewById(R.id.webView1);
		webView.setWebViewClient(new NetflixWebViewClient());
		webView.getSettings().setJavaScriptEnabled(true);
		webView.loadUrl("http://www.netflix.com?preventIntent=true");
	}

    @Override
    protected void onStart() {
        super.onStart();

        logger.info("Starting onStart");

        Preferences preferences = new Preferences(this);
        if (!preferences.isConfigured()) {
            Dialog dialog = createSettingsMissingDialog(getString(R.string.missing_connection_details));
            dialog.show();
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

    private AlertDialog createSettingsMissingDialog(String message) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);

        builder.setMessage(message)
                .setCancelable(true)
                .setPositiveButton(R.string.settings_label,
                        new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int id) {
                                startActivity(new Intent(
                                        NetflixWebViewActivity.this,
                                        PrefsActivity.class));
                            }
                        });
        return builder.create();
    }
}