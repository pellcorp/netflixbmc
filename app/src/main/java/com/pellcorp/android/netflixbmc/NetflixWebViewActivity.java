package com.pellcorp.android.netflixbmc;

import android.app.Activity;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.Intent;
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

public class NetflixWebViewActivity extends Activity implements NetflixWebViewClientListener {
    private final Logger logger = LoggerFactory.getLogger(getClass().getName());

	private WebView webView;
    private ProgressDialog progressDialog;

    public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.webview);

        Preferences preferences = new Preferences(this);
        if (preferences.isConfigured()) {
            progressDialog = ProgressDialog.show(
                    this, getString(R.string.logging_in), getString(R.string.please_wait));

            String username = preferences.getString(R.string.pref_netflix_username);
            String password = preferences.getString(R.string.pref_netflix_password);
            NetflixLogin login = new NetflixLogin();
            if (login.login(username, password)) {
                //progressDialog.dismiss();

                CookieSyncManager.createInstance(this);
                CookieManager cookieManager = CookieManager.getInstance();
                List<Cookie> cookies = login.getCookieStore().getCookies();
                for (Cookie cookie : cookies) {
                    String cookieString = cookie.getName() + "=" + cookie.getValue() + "; domain=" + cookie.getDomain();
                    cookieManager.setCookie("netflix.com", cookieString);
                    CookieSyncManager.getInstance().sync();
                }
                webView = (WebView) findViewById(R.id.webView1);

                String url = preferences.getString(R.string.pref_host_url);
                JsonClient jsonClient = new JsonClientImpl(url);

                MovieIdSender sender = new MovieIdSender(jsonClient);
                webView.setWebViewClient(new NetflixWebViewClient(this, sender));
                webView.getSettings().setJavaScriptEnabled(true);
                webView.loadUrl("http://www.netflix.com");

            } else {
                progressDialog.dismiss();

                Dialog dialog = ActivityUtils.createErrorDialog(this,
                        getString(R.string.login_failed));
                dialog.show();
            }
        } else {
            Dialog dialog = ActivityUtils.createSettingsMissingDialog(this, getString(R.string.missing_connection_details));
            dialog.show();
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
                startActivity(new Intent(this, PreferenceFragment.class));
                return true;
        }
        return false;
    }

    @Override
    public void onPageStart(String url) {

    }

    @Override
    public void onPageFinished(String url) {
        if (progressDialog.isShowing()) {
            progressDialog.dismiss();
        }
    }
}