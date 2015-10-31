package com.pellcorp.android.netflixbmc;

import android.app.Activity;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.webkit.CookieManager;
import android.webkit.CookieSyncManager;
import android.webkit.WebView;

import com.pellcorp.android.netflixbmc.jsonrpc.JsonClient;
import com.pellcorp.android.netflixbmc.jsonrpc.JsonClientImpl;
import com.pellcorp.android.netflixbmc.jsonrpc.MovieIdSender;

import org.apache.http.cookie.Cookie;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;

public class NetflixWebViewActivity extends Activity {
    private final Logger logger = LoggerFactory.getLogger(getClass().getName());

	private WebView webView;

    public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.webview);

        Preferences preferences = new Preferences(this);
        if (preferences.isConfigured()) {
            String username = preferences.getString(R.string.pref_netflix_username);
            String password = preferences.getString(R.string.pref_netflix_password);

            String url = preferences.getString(R.string.pref_host_url);
            JsonClient jsonClient = new JsonClientImpl(url);

            MovieIdSender sender = new MovieIdSender(jsonClient);
            NetflixWebViewClient viewClient = new NetflixWebViewClient(this, sender);

            webView = (WebView) findViewById(R.id.webView1);
            webView.setWebViewClient(viewClient);
            webView.getSettings().setJavaScriptEnabled(true);

            AsyncTask<String, Void, Boolean> loadNetflixTask = new AsyncTask<String, Void, Boolean>() {
                ProgressDialog asyncDialog = new ProgressDialog(NetflixWebViewActivity.this);

                @Override
                protected void onPreExecute() {
                    asyncDialog.setMessage(getString(R.string.logging_in));
                    asyncDialog.show();
                    super.onPreExecute();
                }

                @Override
                protected Boolean doInBackground(String... params) {
                    String email = params[0];
                    String password = params[1];
                    return doLogin(email, password);
                }

                @Override
                protected void onPostExecute(Boolean result) {
                    asyncDialog.dismiss();
                    postExecute(result);
                    super.onPostExecute(result);
                }
            };

            loadNetflixTask.execute(username, password);
        } else {
            Dialog dialog = ActivityUtils.createSettingsMissingDialog(this, getString(R.string.missing_connection_details));
            dialog.show();
        }
	}

    private void postExecute(Boolean result) {
        if (result) {

            webView.loadUrl("http://www.netflix.com");
        } else {
            Dialog dialog = ActivityUtils.createErrorDialog(
                    this,
                    getString(R.string.login_failed));
            dialog.show();
        }
    }

    private boolean doLogin(String email, String password) {
        NetflixLogin login = new NetflixLogin();
        if (login.login(email, password)) {
            CookieSyncManager.createInstance(this);
            CookieManager cookieManager = CookieManager.getInstance();
            List<Cookie> cookies = login.getCookieStore().getCookies();

            for (Cookie cookie : cookies) {
                String cookieString = cookie.getName() + "=" + cookie.getValue() + "; domain=" + cookie.getDomain();
                cookieManager.setCookie("netflix.com", cookieString);
                CookieSyncManager.getInstance().sync();
            }
            return true;
        } else {
            return false;
        }
    }

    @Override
    protected void onStart() {
        super.onStart();

        logger.info("Starting onStart");

        Preferences preferences = new Preferences(this);
        if (!preferences.isConfigured()) {
            Dialog dialog = ActivityUtils.createSettingsMissingDialog(this, getString(R.string.missing_connection_details));
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
                startActivity(new Intent(this, PreferenceActivity.class));
                return true;
        }
        return false;
    }
}