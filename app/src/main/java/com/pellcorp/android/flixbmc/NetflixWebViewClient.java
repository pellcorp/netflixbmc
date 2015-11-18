package com.pellcorp.android.flixbmc;

import android.app.Activity;
import android.app.ProgressDialog;
import android.graphics.Bitmap;
import android.webkit.WebResourceResponse;
import android.webkit.WebView;
import android.webkit.WebViewClient;

import com.pellcorp.android.flixbmc.web.NetflixClientException;
import com.pellcorp.android.flixbmc.web.NetflixUrl;
import com.pellcorp.android.flixbmc.web.NetflixWebViewClientServiceProvider;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class NetflixWebViewClient extends WebViewClient {
    private final Logger logger = LoggerFactory.getLogger(getClass().getName());

    private final NetflixWebViewClientServiceProvider client;
    private final ProgressDialogs progressDialog;

    public NetflixWebViewClient(final NetflixWebViewClientServiceProvider client,
                                final ProgressDialogs progressDialog) {
        this.client = client;
        this.progressDialog = progressDialog;
    }

    @Override
    public WebResourceResponse shouldInterceptRequest(WebView view, String url) {
        try {
            return client.loadUrl(url);
        } catch (NetflixClientException e) {
            logger.error("Failed to load url: " + url, e);
            return null;
        }
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
        super.onPageStarted(view, url, favicon);

        progressDialog.show();
    }

    @Override
    public void onPageFinished(WebView view, String url) {
        super.onPageFinished(view, url);

        progressDialog.dismiss();
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