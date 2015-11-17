package com.pellcorp.android.flixbmc;

import android.content.Context;
import android.webkit.WebResourceResponse;

/**
 * Created by jason on 17/11/15.
 */
public interface NetflixWebViewClientServiceProvider {
    void sendToKodi(String url);
    WebResourceResponse loadUrl(String url);
    String getString(int id);
}
