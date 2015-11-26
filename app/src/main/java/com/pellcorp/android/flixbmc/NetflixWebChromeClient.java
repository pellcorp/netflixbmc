package com.pellcorp.android.flixbmc;

import android.webkit.WebChromeClient;
import android.webkit.WebView;

/**
 * Created by jason on 27/11/15.
 */
public class NetflixWebChromeClient extends WebChromeClient {
    private final ProgressSpinner progressSpinner;

    public NetflixWebChromeClient(final NetflixWebViewActivity activity) {
        this.progressSpinner = activity;
    }

    @Override
    public void onProgressChanged(WebView view, int newProgress) {
        if (newProgress > 50) {
            progressSpinner.hideSpinner();
        }
        super.onProgressChanged(view, newProgress);
    }
}
