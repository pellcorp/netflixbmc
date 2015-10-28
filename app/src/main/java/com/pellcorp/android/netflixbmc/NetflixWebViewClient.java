package com.pellcorp.android.netflixbmc;

import android.app.Activity;
import android.app.Dialog;
import android.graphics.Bitmap;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.Toast;

import com.pellcorp.android.netflixbmc.jsonrpc.JsonClientResponse;
import com.pellcorp.android.netflixbmc.jsonrpc.MovieIdSender;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class NetflixWebViewClient extends WebViewClient {
    private final Logger logger = LoggerFactory.getLogger(getClass().getName());

    private final Activity activity;
    private final MovieIdSender sender;

    public NetflixWebViewClient(Activity activity, MovieIdSender sender) {
        this.activity = activity;
        this.sender = sender;
    }

    @Override
    public boolean shouldOverrideUrlLoading(WebView view, String url) {
        logger.debug("shouldOverrideUrlLoading: {}", url);

        if (url.contains("://www.netflix.com/watch/")) {
            logger.debug("Sending Watch request to Kodi: {}", url);
            JsonClientResponse result = sender.sendMovie(url);

            if (result.isSuccess()) {
                Toast.makeText(activity, R.string.successful_submission, Toast.LENGTH_SHORT).show();
            } else if (result.isError()) {
                Dialog dialog = ActivityUtils.createErrorDialog(activity, result.getErrorMessage());
                dialog.show();
            }
            return true;
        } else {
            return false;
        }
    }
}