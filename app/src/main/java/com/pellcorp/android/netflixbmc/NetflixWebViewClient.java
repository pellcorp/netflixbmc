package com.pellcorp.android.netflixbmc;

import android.app.Activity;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.graphics.Bitmap;
import android.view.View;
import android.webkit.WebResourceResponse;
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

    private MovieIdSender sender;
    private final ProgressDialog progressDialog;

    public NetflixWebViewClient(Activity activity) {
        this.progressDialog = new ProgressDialog(activity);
    }

    public void setSender(MovieIdSender sender) {
        this.sender = sender;
    }

    @Override
    public WebResourceResponse shouldInterceptRequest(WebView view, String url) {
        if (url.contains("://www.netflix.com/")) {
            return null;
        } else {
            return null;
        }
    }

    @Override
    public boolean shouldOverrideUrlLoading(WebView view, String url) {
        String originalUrl = view.getOriginalUrl();

        logger.debug("shouldOverrideUrlLoading: {}", url);

        if (url.contains("://www.netflix.com/watch/")) {
            logger.debug("Sending Watch request to Kodi: {}", url);
            if (sender != null) {
                sender.sendMovie(url);
            } else {
                logger.error("No sender registered");
            }
            return true;
        } else {
            view.loadUrl(url);
            return true;
        }
    }

    @Override
    public void onLoadResource(WebView view, String url) {
        String originalUrl = view.getOriginalUrl();

        if (url.contains("://www.netflix.com/")) {
            super.onLoadResource(view, url);
        } else {
            super.onLoadResource(view, url);
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