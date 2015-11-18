package com.pellcorp.android.flixbmc;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.WindowManager;
import android.webkit.CookieManager;
import android.webkit.CookieSyncManager;
import android.webkit.WebResourceResponse;
import android.webkit.WebView;

import static com.pellcorp.android.flixbmc.ActivityUtils.DialogType.OK_FINISH;
import static com.pellcorp.android.flixbmc.ActivityUtils.DialogType.OK_RECREATE;

import com.pellcorp.android.flixbmc.web.HttpClientProvider;
import com.pellcorp.android.flixbmc.web.HttpClientProviderImpl;
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
    private SharedPreferences.OnSharedPreferenceChangeListener preferenceChangeListener = null;
    private ProgressDialogs progressDialog;

    private NetflixClient netflixClient;

    private NetflixEndpoint netflixEndpoint = NetflixEndpoint.DEFAULT;

    public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

        setContentView(R.layout.webview);

        webView = (WebView) findViewById(R.id.webView);
        webView.setBackgroundColor(Color.TRANSPARENT);

        webView.getSettings().setJavaScriptEnabled(true);
        webView.getSettings().setUserAgentString(UserAgents.Mobile);

        progressDialog = new ProgressDialogs(this);
        NetflixWebViewClient viewClient = new NetflixWebViewClient(this, progressDialog);
        webView.setWebViewClient(viewClient);

        HttpClientProvider clientProvider = new HttpClientProviderImpl(this);
        netflixClient = new NetflixClientImpl(clientProvider.getHttpClient());

        Preferences preferences = new Preferences(this);

        preferenceChangeListener = new SharedPreferences.OnSharedPreferenceChangeListener() {
            public void onSharedPreferenceChanged(SharedPreferences prefs, String key) {
                if(getString(R.string.pref_netflix_username).equals(key)
                        || getString(R.string.pref_netflix_password).equals(key)) {
                    tryLoginWithProgressDialog(false);
                }
            }
        };

        preferences.registerOnPreferenceChangeListener(preferenceChangeListener);

        if(savedInstanceState != null) {
            webView.restoreState(savedInstanceState);
        }
	}

    @Override
    protected void onRestoreInstanceState(Bundle savedInstanceState) {
        super.onRestoreInstanceState(savedInstanceState);

        webView.restoreState(savedInstanceState);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();

        if (webView != null) {
            webView.destroy();
        }
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
    public void onBackPressed() {
        if (webView.canGoBack()) {
            webView.goBack();
            return;
        } else {
            createConfirmExitDialog();
        }
    }

    public void createConfirmExitDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(NetflixWebViewActivity.this);

        builder.setMessage(R.string.confirm_exit_message);

        builder.setPositiveButton(R.string.yes, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                dialog.dismiss();
                NetflixWebViewActivity.this.finish();
            }
        });

        builder.setNegativeButton(R.string.no, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                dialog.dismiss();
            }
        });
        AlertDialog dialog = builder.create();
        dialog.show();
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
    protected void onPause() {
        super.onPause();

        pausedState = null;
        if (webView.getOriginalUrl() != null) {
            pausedState = new Bundle();
            webView.saveState(pausedState);
        }
    }

    @Override
    protected void onResume() {
        super.onResume();

        // we do not want to trigger a reload of the web view when we send out the SendToKodiActivity,
        // not sure how to handle that.
        if (pausedState == null) {
            tryLoginWithProgressDialog(true);
        }
    }

    private void tryLoginWithProgressDialog(final boolean isWelcome) {
        Preferences preferences = new Preferences(this);
        String username = preferences.getString(R.string.pref_netflix_username);
        String password = preferences.getString(R.string.pref_netflix_password);

        if (username != null && password != null) {
            AsyncTask<String, Void, LoginResponse> loadNetflixTask = new AsyncTask<String, Void, LoginResponse>() {
                @Override
                protected void onPreExecute() {
                    progressDialog.show(isWelcome);
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

                    super.onPostExecute(result);
                }
            };

            loadNetflixTask.execute(username, password);
        } else {
            progressDialog.dismiss();

            ActivityUtils.createSettingsMissingDialog(this, getString(R.string.invalid_netflix_settings));
        }
    }

    private LoginResponse doLogin(String email, String password) {
        CookieManager cookieManager = CookieManager.getInstance();
        cookieManager.removeAllCookie();

        LoginResponse state = netflixClient.login(email, password);

        if (state.isSuccessful()) {
            CookieSyncManager syncManager = CookieSyncManager.createInstance(this);
            List<Cookie> cookies = netflixClient.getCookieStore().getCookies();

            for (Cookie cookie : cookies) {
                String cookieString = cookie.getName() + "=" + cookie.getValue() + "; domain=" + cookie.getDomain();
                cookieManager.setCookie(".netflix.com", cookieString);
                syncManager.sync();
            }
            return state;
        } else {
            return state;
        }
    }

    private void postLoginExecute(LoginResponse result) {
        if (result.isSuccessful()) {
            // we don't dismiss the dialog, as we will reuse it in loading first page
            webView.loadUrl(netflixEndpoint.getHomePage());
        } else {
            progressDialog.dismiss();

            if (result.isInvalidCredentials()) {
                ActivityUtils.createSettingsMissingDialog(this, R.string.netflix_invalid_credentials);
            } else if (result.isUnableToProcessRequest()) {
                ActivityUtils.createErrorDialog(this, R.string.netflix_not_responding, OK_RECREATE);
            } else {
                ActivityUtils.createErrorDialog(this, result.getFailureReason(), OK_FINISH);
            }
        }
    }
}
