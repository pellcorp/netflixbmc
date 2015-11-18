package com.pellcorp.android.flixbmc.web;

import android.content.Context;
import android.net.SSLCertificateSocketFactory;
import android.net.SSLSessionCache;

import org.apache.http.client.HttpClient;
import org.apache.http.conn.ClientConnectionManager;
import org.apache.http.conn.scheme.PlainSocketFactory;
import org.apache.http.conn.scheme.Scheme;
import org.apache.http.conn.scheme.SchemeRegistry;
import org.apache.http.conn.scheme.SocketFactory;
import org.apache.http.impl.client.AbstractHttpClient;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.impl.conn.tsccm.ThreadSafeClientConnManager;
import org.apache.http.params.HttpConnectionParams;
import org.apache.http.params.HttpParams;
import org.apache.http.params.HttpProtocolParams;

/*
http://candrews.integralblue.com/2011/09/best-way-to-use-httpclient-in-android/
 */
public class HttpClientProviderImpl implements HttpClientProvider {
    // Wait this many milliseconds max for the TCP connection to be established
    private static final int CONNECTION_TIMEOUT = 60 * 1000;

    // Wait this many milliseconds max for the server to send us data once the connection has been established
    private static final int SO_TIMEOUT = 5 * 60 * 1000;

    private final Context context;

    public HttpClientProviderImpl(final Context context) {
        this.context = context;
    }

    @Override
    public HttpClient getHttpClient() {
        AbstractHttpClient client = new DefaultHttpClient() {
            @Override
            protected ClientConnectionManager createClientConnectionManager() {
                SchemeRegistry registry = new SchemeRegistry();
                registry.register(
                        new Scheme("http", PlainSocketFactory.getSocketFactory(), 80));
                registry.register(
                        new Scheme("https", getHttpsSocketFactory(context), 443));
                HttpParams params = getParams();
                HttpConnectionParams.setConnectionTimeout(params, CONNECTION_TIMEOUT);
                HttpConnectionParams.setSoTimeout(params, SO_TIMEOUT);
                HttpProtocolParams.setUserAgent(params, UserAgents.Mobile);
                return new ThreadSafeClientConnManager(params, registry);
            }

            private SocketFactory getHttpsSocketFactory(final Context context) {
                SSLSessionCache sessionCache = new SSLSessionCache(context);
                SocketFactory socketFactory = SSLCertificateSocketFactory.getHttpSocketFactory(CONNECTION_TIMEOUT, sessionCache);
                return socketFactory;
            }
        };

        return client;
    }
}
