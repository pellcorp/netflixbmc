package com.pellcorp.android.flixbmc;

import android.app.Activity;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.Intent;
import android.content.res.Configuration;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.webkit.CookieManager;
import android.webkit.CookieSyncManager;
import android.webkit.WebResourceResponse;
import android.webkit.WebView;

import org.apache.http.cookie.Cookie;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;

public class NetflixWebViewActivity extends Activity implements NetflixWebViewClientServiceProvider {
    private final Logger logger = LoggerFactory.getLogger(getClass().getName());

    private WebView webView;
    private Bundle pausedState;
    private NetflixClient netflixClient;

    public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		setContentView(R.layout.webview);

        webView = (WebView) findViewById(R.id.webView1);

        webView.getSettings().setJavaScriptEnabled(true);
        webView.getSettings().setUserAgentString("Mozilla/5.0 (Linux; Android 4.4.2; Android SDK built for x86 Build/KK) AppleWebKit/537.36 (KHTML, like Gecko) Version/4.0 Chrome/30.0.0.0 Mobile Safari/537.36");

        NetflixWebViewClient viewClient = new NetflixWebViewClient(this);

        webView.setWebViewClient(viewClient);

        webView.restoreState(savedInstanceState);

        netflixClient = new NetflixClientImpl();
	}

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
    }

    @Override
    protected void onRestoreInstanceState(Bundle savedInstanceState) {
        super.onRestoreInstanceState(savedInstanceState);

        // Restore the state of the WebView
        webView.restoreState(savedInstanceState);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if (webView != null)
            webView.destroy();
    }

    @Override
    public void sendToKodi(String url) {
        Intent sendToKodi = new Intent(this, SendToKodiActivity.class);
        sendToKodi.putExtra(SendToKodiActivity.NETFLIX_URL, url);
        sendToKodi.setAction(SendToKodiActivity.SEND_TO_KODI);
        startActivity(sendToKodi);

    }

    @Override
    public WebResourceResponse loadUrl(String url) {
        // the call to this method is done in a non UI thread, so no need for an async task
        return netflixClient.loadUrl(url);
    }

    @Override
    protected void onStart() {
        super.onStart();
    }

    @Override
    protected void onPause() {
        super.onPause();

        pausedState = new Bundle();
        webView.saveState(pausedState);
    }

    @Override
    public void onBackPressed() {
        if (webView.canGoBack()) {
            webView.goBack();
            return;
        }
        super.onBackPressed();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        super.onCreateOptionsMenu(menu);
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.menu, menu);
        return true;
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        webView.saveState(outState);
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

    @Override
    protected void onResume() {
        super.onResume();

        // we do not want to trigger a reload of the web view when we send out the SendToKodiActivity,
        // not sure how to handle that.
        if (pausedState == null) {
            Preferences preferences = new Preferences(this);

            String username = preferences.getString(R.string.pref_netflix_username);
            String password = preferences.getString(R.string.pref_netflix_password);

            if (username != null && password != null) {
                AsyncTask<String, Void, LoginResponse> loadNetflixTask = new AsyncTask<String, Void, LoginResponse>() {
                    ProgressDialog progressDialog = new ProgressDialog(NetflixWebViewActivity.this);

                    @Override
                    protected void onPreExecute() {
                        progressDialog.setMessage(getString(R.string.logging_in));
                        progressDialog.show();
                    }

                    @Override
                    protected LoginResponse doInBackground(String... params) {
                        String email = params[0];
                        String password = params[1];
                        return doLogin(email, password);
                    }

                    @Override
                    protected void onPostExecute(LoginResponse result) {
                        postLoginExecute(result);
                        progressDialog.dismiss();
                        super.onPostExecute(result);
                    }
                };

                loadNetflixTask.execute(username, password);
            } else {
                Dialog dialog = ActivityUtils.createSettingsMissingDialog(this,
                        getString(R.string.missing_settings), true);
                dialog.show();
            }
        }
    }

    private LoginResponse doLogin(String email, String password) {
        LoginResponse state = netflixClient.login(email, password);
        if (state.isSuccessful()) {
            return state;
        } else {
            return state;
        }
    }

    private void postLoginExecute(LoginResponse result) {
        if (result.isSuccessful()) {
            webView.loadUrl("https://www.netflix.com");
        } else {
            Dialog dialog = ActivityUtils.createErrorDialog(
                    this,
                    getString(R.string.login_failed),
                    result.getFailureReason(),
                    true);
            dialog.show();
        }
    }
}
