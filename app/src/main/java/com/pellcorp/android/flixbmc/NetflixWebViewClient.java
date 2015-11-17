package com.pellcorp.android.flixbmc;

import android.app.Activity;
import android.app.ProgressDialog;
import android.graphics.Bitmap;
import android.webkit.WebResourceResponse;
import android.webkit.WebView;
import android.webkit.WebViewClient;

import com.pellcorp.android.flixbmc.web.NetflixUrl;
import com.pellcorp.android.flixbmc.web.NetflixWebViewClientServiceProvider;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class NetflixWebViewClient extends WebViewClient {
    private final Logger logger = LoggerFactory.getLogger(getClass().getName());

    private final ProgressDialog progressDialog;
    private final NetflixWebViewClientServiceProvider client;

    public NetflixWebViewClient(final NetflixWebViewClientServiceProvider client) {
        this.client = client;
        this.progressDialog = new ProgressDialog((Activity) client);
    }

    @Override
    public WebResourceResponse shouldInterceptRequest(WebView view, String url) {
        return client.loadUrl(url);
    }

    @Override
    public boolean shouldOverrideUrlLoading(WebView view, String url) {
        String originalUrl = view.getOriginalUrl();

        logger.debug("shouldOverrideUrlLoading: {}", url);

        if(checkUrl(url)) {
            return true;
        } else if(checkUrl(originalUrl)) {
            return true;
        }

        view.loadUrl(url);
        return true;
    }

    @Override
    public void onPageStarted(WebView view, String url, Bitmap favicon) {
        String originalUrl = view.getOriginalUrl();

        if (!progressDialog.isShowing()) {
            progressDialog.show();
        }

        progressDialog.setMessage(progressDialog.getContext().getString(R.string.loading));

        super.onPageStarted(view, url, favicon);
    }

    @Override
    public void onPageFinished(WebView view, String url) {
        String originalUrl = view.getOriginalUrl();

        super.onPageFinished(view, url);

        if (progressDialog.isShowing()) {
            progressDialog.dismiss();
        }
    }

    private boolean checkUrl(String url) {
        if(url == null) {
            return false;
        }

        NetflixUrl netflixUrl = new NetflixUrl(url);
        if (!netflixUrl.isWatch()) {
            return false;
        }

        logger.debug("Sending Watch request to Kodi: {}", url);
        client.sendToKodi(url);

        return true;
    }
}