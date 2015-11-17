package com.pellcorp.android.flixbmc;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Intent;
import android.graphics.Bitmap;
import android.webkit.WebView;
import android.webkit.WebViewClient;

import com.pellcorp.android.flixbmc.jsonrpc.MovieIdSender;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class NetflixWebViewClient extends WebViewClient {
    private final Logger logger = LoggerFactory.getLogger(getClass().getName());

    private final ProgressDialog progressDialog;
    private final Activity activity;

    public NetflixWebViewClient(Activity activity) {
        this.activity = activity;
        this.progressDialog = new ProgressDialog(activity);
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
        Intent sendToKodi = new Intent(activity, SendToKodiActivity.class);
        sendToKodi.putExtra(SendToKodiActivity.NETFLIX_URL, url);
        sendToKodi.setAction(SendToKodiActivity.SEND_TO_KODI);
        activity.startActivity(sendToKodi);

        return true;
    }
}