package com.pellcorp.android.netflixbmc;

import android.app.Activity;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.graphics.Bitmap;
import android.view.View;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.pellcorp.android.netflixbmc.jsonrpc.JsonClientResponse;
import com.pellcorp.android.netflixbmc.jsonrpc.MovieIdSender;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class NetflixWebViewClient extends WebViewClient {
    private final Logger logger = LoggerFactory.getLogger(getClass().getName());

    private final MovieIdSender sender;
    private final ProgressDialog progressDialog;

    public NetflixWebViewClient(ProgressDialog progressDialog, MovieIdSender sender) {
        this.progressDialog = progressDialog;
        this.sender = sender;
    }

    @Override
    public boolean shouldOverrideUrlLoading(WebView view, String url) {
        logger.debug("shouldOverrideUrlLoading: {}", url);

        if (url.contains("://www.netflix.com/watch/")) {
            logger.debug("Sending Watch request to Kodi: {}", url);
            sender.sendMovie(url);
            return true;
        } else {
            view.loadUrl(url);
            return true;
        }
    }

    @Override
    public void onLoadResource(WebView view, String url) {
        if (url.contains("://www.netflix.com/")) {
            super.onLoadResource(view, url);
        } else {
            super.onLoadResource(view, url);
        }
    }

    @Override
    public void onPageStarted(WebView view, String url, Bitmap favicon) {
        progressDialog.setMessage(progressDialog.getContext().getString(R.string.loading));
        super.onPageStarted(view, url, favicon);
    }

    @Override
    public void onPageFinished(WebView view, String url) {
        super.onPageFinished(view, url);

        if (progressDialog.isShowing()) {
            progressDialog.dismiss();
        }
    }
}