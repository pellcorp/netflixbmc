package com.pellcorp.android.flixbmc.web;

import android.webkit.WebResourceResponse;

import org.apache.http.client.CookieStore;

public interface NetflixClient {
    CookieStore getCookieStore();
    boolean isLoggedIn();
    LoginResponse login(String email, String password);
    WebResourceResponse loadUrl(String url);
}
