package com.pellcorp.android.flixbmc;

import android.app.Activity;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.Intent;
import android.content.SharedPreferences;
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

import com.pellcorp.android.flixbmc.web.LoginResponse;
import com.pellcorp.android.flixbmc.web.NetflixClient;
import com.pellcorp.android.flixbmc.web.NetflixClientImpl;
import com.pellcorp.android.flixbmc.web.NetflixWebViewClientServiceProvider;
import com.pellcorp.android.flixbmc.web.UserAgents;

import org.apache.http.cookie.Cookie;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;

public class NetflixWebViewActivity extends Activity implements NetflixWebViewClientServiceProvider {
    private final Logger logger = LoggerFactory.getLogger(getClass().getName());

    private WebView webView;
    private Bundle pausedState;
    private NetflixClient netflixClient;
    private SharedPreferences.OnSharedPreferenceChangeListener preferenceChangeListener = null;

    public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		setContentView(R.layout.webview);

        webView = (WebView) findViewById(R.id.webView1);

        webView.getSettings().setJavaScriptEnabled(true);
        webView.getSettings().setUserAgentString(UserAgents.Mobile);

        NetflixWebViewClient viewClient = new NetflixWebViewClient(this);

        webView.setWebViewClient(viewClient);

        netflixClient = new NetflixClientImpl();

        Preferences preferences = new Preferences(this);

        preferenceChangeListener = new SharedPreferences.OnSharedPreferenceChangeListener() {
            public void onSharedPreferenceChanged(SharedPreferences prefs, String key) {
                if(getString(R.string.pref_netflix_username).equals(key)
                        || getString(R.string.pref_netflix_password).equals(key)) {
                    tryLoginWithProgressDialog();
                }
            }
        };

        preferences.registerOnPreferenceChangeListener(preferenceChangeListener);

        if(savedInstanceState != null) {
            webView.restoreState(savedInstanceState);
        }
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
            tryLoginWithProgressDialog();
        }
    }

    private void tryLoginWithProgressDialog() {
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
                    getString(R.string.missing_settings), false);
            dialog.show();
        }
    }

    private LoginResponse doLogin(String email, String password) {
        CookieManager cookieManager = CookieManager.getInstance();
        cookieManager.removeAllCookie();

        LoginResponse state = netflixClient.login(email, password);

        if (state.isSuccessful()) {
            CookieSyncManager.createInstance(this);
            List<Cookie> cookies = netflixClient.getCookieStore().getCookies();

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

    private void postLoginExecute(LoginResponse result) {
        if (result.isSuccessful()) {
            webView.loadUrl("https://www.netflix.com");
        } else {
            String htmlWrongCredentials = getString(R.string.netflix_wrong_credentials_html);

            webView.loadData(htmlWrongCredentials, "text/html", null);

            Dialog dialog = ActivityUtils.createErrorDialog(
                    this,
                    getString(R.string.login_failed),
                    result.getFailureReason(),
                    true);
            dialog.show();
        }
    }

}
