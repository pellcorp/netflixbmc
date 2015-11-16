package com.pellcorp.android.flixbmc;

import android.app.Activity;
import android.app.ProgressDialog;
import android.graphics.Bitmap;
import android.webkit.WebView;
import android.webkit.WebViewClient;

import com.pellcorp.android.flixbmc.jsonrpc.MovieIdSender;

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
    public boolean shouldOverrideUrlLoading(WebView view, String url) {
        String originalUrl = null;
        if(view != null)
            originalUrl = view.getOriginalUrl();

        if(url.contains("://www.netflix.com/title/") )
            view.getSettings().setUserAgentString(UserAgents.Desktop);
        else
            view.getSettings().setUserAgentString(UserAgents.Mobile);

        if(checkUrl(url) )
            return true;
        else if(checkUrl(originalUrl))
            return true;

        view.loadUrl(url);
        return true;
    }

    @Override
    public void onPageStarted(WebView view, String url, Bitmap favicon) {
        if (!progressDialog.isShowing()) {
            progressDialog.show();
        }

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

    private boolean checkUrl(String url)
    {
        if(url == null)
            return false;

        if(!url.contains("://www.netflix.com/watch/") )
            return false;

        logger.debug("Sending Watch request to Kodi: {}", url);
        if (sender != null) {
            sender.sendMovie(url);
        } else {
            logger.error("No sender registered");
        }
        return true;
    }
}