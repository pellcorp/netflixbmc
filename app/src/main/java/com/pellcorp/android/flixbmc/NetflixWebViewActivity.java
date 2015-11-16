package com.pellcorp.android.flixbmc;

import android.app.Activity;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.webkit.CookieManager;
import android.webkit.CookieSyncManager;
import android.webkit.WebView;

import org.apache.http.cookie.Cookie;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;

public class NetflixWebViewActivity extends Activity {
    private final Logger logger = LoggerFactory.getLogger(getClass().getName());

	private WebView webView;
    private Bundle pausedState;

    public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

        if(savedInstanceState != null)
            webView.restoreState(savedInstanceState);

		setContentView(R.layout.webview);

        webView = (WebView) findViewById(R.id.webView1);

        webView.getSettings().setJavaScriptEnabled(true);
        webView.getSettings().setUserAgentString(UserAgents.Mobile);

        NetflixWebViewClient viewClient = new NetflixWebViewClient(this);
        webView.setWebViewClient(viewClient);
	}

    private LoginState doLogin(String email, String password) {
        NetflixLogin login = new NetflixLogin();
        LoginState state = login.login(email, password);
        if (state.isSuccessful()) {
            CookieSyncManager.createInstance(this);
            CookieManager cookieManager = CookieManager.getInstance();
            List<Cookie> cookies = login.getCookieStore().getCookies();

            for (Cookie cookie : cookies) {
                String cookieString = cookie.getName() + "=" + cookie.getValue() + "; domain=" + cookie.getDomain();
                cookieManager.setCookie("netflix.com", cookieString);
                CookieSyncManager.getInstance().sync();
            }
            return state;
        } else {
            return state;
        }
    }

    @Override
    protected void onStart() {
        super.onStart();

        logger.info("Starting onStart");
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
    protected void onResume() {
        super.onResume();

        // we do not want to trigger a reload of the web view when we send out the SendToKodiActivity,
        // not sure how to handle that.
        if (pausedState == null) {
            Preferences preferences = new Preferences(this);
            String username = preferences.getString(R.string.pref_netflix_username);
            String password = preferences.getString(R.string.pref_netflix_password);

            if (username != null && password != null) {
                AsyncTask<String, Void, LoginState> loadNetflixTask = new AsyncTask<String, Void, LoginState>() {
                    ProgressDialog progressDialog = new ProgressDialog(NetflixWebViewActivity.this);

                    @Override
                    protected void onPreExecute() {
                        progressDialog.setMessage(getString(R.string.logging_in));
                        progressDialog.show();
                    }

                    @Override
                    protected LoginState doInBackground(String... params) {
                        String email = params[0];
                        String password = params[1];
                        return doLogin(email, password);
                    }

                    @Override
                    protected void onPostExecute(LoginState result) {
                        postExecute(result);
                        progressDialog.dismiss();
                        super.onPostExecute(result);
                    }
                };

                loadNetflixTask.execute(username, password);
            } else {
                Dialog dialog = ActivityUtils.createSettingsMissingDialog(this,
                        getString(R.string.missing_netflix_credentials), false);
                dialog.show();
            }
        }
    }

    private void postExecute(LoginState result) {
        if (result.isSuccessful()) {
            webView.loadUrl("http://www.netflix.com");
        } else {
            Dialog dialog = ActivityUtils.createErrorDialog(
                    this,
                    getString(R.string.login_failed),
                    result.getFailureReason(),
                    false);
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
}