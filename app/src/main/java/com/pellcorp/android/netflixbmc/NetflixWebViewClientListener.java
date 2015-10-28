package com.pellcorp.android.netflixbmc;

/**
 * Created by jason on 28/10/15.
 */
public interface NetflixWebViewClientListener {
    void onPageStart(String url);
    void onPageFinished(String url);
}
