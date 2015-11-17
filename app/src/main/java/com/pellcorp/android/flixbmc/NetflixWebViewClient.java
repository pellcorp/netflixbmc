package com.pellcorp.android.flixbmc;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Intent;
import android.graphics.Bitmap;
import android.webkit.WebResourceResponse;
import android.webkit.WebView;
import android.webkit.WebViewClient;

import com.pellcorp.android.flixbmc.jsonrpc.MovieIdSender;

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

        if (url.contains("://www.netflix.com/watch/")) {
            logger.debug("Sending Watch request to Kodi: {}", url);

            client.sendToKodi(url);
            return true;
        } else {
            view.loadUrl(url);
            return true;
        }
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
}