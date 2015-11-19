package com.pellcorp.android.flixbmc;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.webkit.CookieManager;
import android.webkit.CookieSyncManager;
import android.webkit.WebResourceResponse;
import android.webkit.WebView;

import static com.pellcorp.android.flixbmc.ActivityUtils.DialogType.OK_FINISH_NO_CANCEL;
import static com.pellcorp.android.flixbmc.ActivityUtils.DialogType.OK_RECREATE;

import com.pellcorp.android.flixbmc.web.HttpClientProvider;
import com.pellcorp.android.flixbmc.web.HttpClientProviderImpl;
import com.pellcorp.android.flixbmc.web.LoginResponse;
import com.pellcorp.android.flixbmc.web.NetflixClient;
import com.pellcorp.android.flixbmc.web.NetflixClientImpl;
import com.pellcorp.android.flixbmc.web.NetflixEndpoint;
import com.pellcorp.android.flixbmc.web.NetflixWebViewClientServiceProvider;
import com.pellcorp.android.flixbmc.web.UserAgents;

import org.apache.http.cookie.Cookie;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;

public class NetflixWebViewActivity extends AbstractProgressActivity implements NetflixWebViewClientServiceProvider {
    private final Logger logger = LoggerFactory.getLogger(getClass().getName());

    private WebView webView;

    private NetflixClient netflixClient;

    private NetflixEndpoint netflixEndpoint = NetflixEndpoint.DEFAULT;

    private Bundle savedInstanceState;

    public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

        setContentView(R.layout.startup);

        this.savedInstanceState = savedInstanceState;

        HttpClientProvider clientProvider = new HttpClientProviderImpl(this);
        netflixClient = new NetflixClientImpl(clientProvider.getHttpClient(), this);
	}

    @Override
    protected void onRestoreInstanceState(Bundle savedInstanceState) {
        super.onRestoreInstanceState(savedInstanceState);

        if (webView != null) {
            webView.restoreState(savedInstanceState);
        }
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
                NetflixWebViewActivity.super.onBackPressed();
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

        if (webView != null) {
            webView.saveState(outState);
        }
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

        if (!netflixClient.isLoggedIn()) {
            tryLoginWithProgressDialog();
        } else {
            initWebView();
        }
    }

    private void initWebView() {
        if (webView == null) {
            setContentView(R.layout.webview);

            webView = (WebView) findViewById(R.id.webView);

            // we want the web view to be transparent until the page is loaded, but for some
            // reason doing this from the manifest or theme does not work.
            webView.setBackgroundColor(getResources().getColor(android.R.color.transparent));

            webView.getSettings().setJavaScriptEnabled(true);
            webView.getSettings().setUserAgentString(UserAgents.Mobile);

            NetflixWebViewClient viewClient = new NetflixWebViewClient(this);
            webView.setWebViewClient(viewClient);

            if (savedInstanceState != null) {
                webView.restoreState(savedInstanceState);
            }

            // we don't dismiss the dialog, as we will reuse it in loading first page
            webView.loadUrl(netflixEndpoint.getHomePage());
        }
    }

    private void tryLoginWithProgressDialog() {
        Preferences preferences = new Preferences(this);
        String username = preferences.getString(R.string.pref_netflix_username);
        String password = preferences.getString(R.string.pref_netflix_password);

        if (username != null && password != null) {
            AsyncTask<String, Void, LoginResponse> loadNetflixTask = new AsyncTask<String, Void, LoginResponse>() {
                @Override
                protected void onPreExecute() {
                    showSpinner();
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
            hideSpinner();

            ActivityUtils.createSettingsMissingDialog(this, getString(R.string.invalid_netflix_settings));
        }
    }

    private LoginResponse doLogin(String email, String password) {
        CookieManager cookieManager = CookieManager.getInstance();
        cookieManager.removeAllCookie();
        CookieSyncManager syncManager = CookieSyncManager.createInstance(this);
        syncManager.sync();

        LoginResponse state = netflixClient.login(email, password);

        if (state.isSuccessful()) {
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
            // we don't hide the spinner here, because we want to continue loading the home page so spinner stays
            initWebView();
        } else {
            hideSpinner();

            if (result.isInvalidCredentials()) {
                ActivityUtils.createSettingsMissingDialog(this, R.string.netflix_invalid_credentials);
            } else if (result.isUnableToProcessRequest()) {
                ActivityUtils.createErrorDialog(this, R.string.netflix_not_responding, OK_RECREATE);
            } else {
                ActivityUtils.createErrorDialog(this, result.getFailureReason(), OK_FINISH_NO_CANCEL);
            }
        }
    }
}
