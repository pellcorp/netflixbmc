package com.pellcorp.android.flixbmc.web;

import org.apache.http.client.HttpClient;

public interface HttpClientProvider {
    HttpClient getHttpClient();
}
