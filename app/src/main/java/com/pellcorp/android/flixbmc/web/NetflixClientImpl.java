package com.pellcorp.android.flixbmc.web;

import android.webkit.WebResourceResponse;

import com.pellcorp.android.flixbmc.R;
import com.pellcorp.android.flixbmc.StringProvider;

import org.apache.http.Header;
import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.ParseException;
import org.apache.http.client.CookieStore;
import org.apache.http.client.HttpClient;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.params.AllClientPNames;
import org.apache.http.client.protocol.ClientContext;
import org.apache.http.conn.ClientConnectionManager;
import org.apache.http.conn.scheme.PlainSocketFactory;
import org.apache.http.conn.scheme.Scheme;
import org.apache.http.conn.scheme.SchemeRegistry;
import org.apache.http.conn.ssl.SSLSocketFactory;
import org.apache.http.impl.client.BasicCookieStore;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.impl.conn.tsccm.ThreadSafeClientConnManager;
import org.apache.http.impl.cookie.BasicClientCookie;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.params.BasicHttpParams;
import org.apache.http.params.HttpConnectionParams;
import org.apache.http.params.HttpParams;
import org.apache.http.protocol.BasicHttpContext;
import org.apache.http.protocol.HttpContext;
import org.apache.http.util.EntityUtils;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.net.ConnectException;
import java.util.ArrayList;
import java.util.List;

public class NetflixClientImpl implements NetflixClient {
    private final Logger logger = LoggerFactory.getLogger(getClass().getName());

    public static final int MAX_AUTH_PAGE_RETRIES = 10;

    public static final String LOGIN_URL = "https://www.netflix.com/Login";

    private final StringProvider stringProvider;
    private final HttpClient client;
    private final CookieStore cookieStore = new BasicCookieStore();
    private boolean isLoggedIn;

    public NetflixClientImpl(HttpClient client, final StringProvider stringProvider) {
        this.client = client;
        this.stringProvider = stringProvider;
    }

    @Override
    public CookieStore getCookieStore() {
        return cookieStore;
    }

    @Override
    public boolean isLoggedIn() {
        return isLoggedIn;
    }

    @Override
    public WebResourceResponse loadUrl(String url) {
        NetflixUrl netflixUrl = new NetflixUrl(url);

        // note due to shouldOverrideUrlLoading, no /watch url ever gets here, so no need to cater for it
        if (netflixUrl.isNetflixUrl()) {
            client.getParams().setParameter(AllClientPNames.USER_AGENT, UserAgents.Mobile);

            if (netflixUrl.isTitle()) {
                client.getParams().setParameter(AllClientPNames.USER_AGENT, UserAgents.Desktop);
                return doLoadUrl(url);
            } else if (netflixUrl.isBrowse() || netflixUrl.isDefault()) {
                return doLoadUrl(url);
            }
        }

        // else
        return null;
    }

    private WebResourceResponse doLoadUrl(String url) {
        try {
            HttpGet get = new HttpGet(url);

            HttpContext localContext = new BasicHttpContext();

            // force skipping the 'use android app or go to web site page'
            BasicClientCookie cookie = new BasicClientCookie("forceWebsite", "true");
            cookie.setDomain(".netflix.com");
            cookie.setPath("/");
            getCookieStore().addCookie(cookie);

            localContext.setAttribute(ClientContext.COOKIE_STORE, cookieStore);

            HttpResponse httpResponse = client.execute(get, localContext);

            String contentType = "text/html";
            Header contentTypeHeader = httpResponse.getEntity().getContentType();
            if (contentTypeHeader != null) {
                contentType = contentTypeHeader.getValue();
            }

            String charSet = EntityUtils.getContentCharSet(httpResponse.getEntity());

            WebResourceResponse response = new WebResourceResponse(contentType, charSet,
                    httpResponse.getEntity().getContent());

            return response;
        } catch (Exception e) {
            logger.error("Failed to load url: " + url, e);

            throw new NetflixClientException(e);
        }
    }

    @Override
    public LoginResponse login(String email, String password) {
        cookieStore.clear();

        try {
            String authUrl = getAuthUrl();
            if (authUrl == null) {
                logger.info("Could not get the login page to show up");
                return new LoginResponse(false, stringProvider.getString(R.string.login_failed));
            }

            HttpPost post = new HttpPost(LOGIN_URL);

            HttpContext localContext = new BasicHttpContext();
            localContext.setAttribute(ClientContext.COOKIE_STORE, cookieStore);

            List<NameValuePair> parameters = new ArrayList<NameValuePair>();
            parameters.add(new BasicNameValuePair("authURL", authUrl));
            parameters.add(new BasicNameValuePair("email", email));
            parameters.add(new BasicNameValuePair("password", password));
            parameters.add(new BasicNameValuePair("RememberMe", "on"));

            UrlEncodedFormEntity entity = new UrlEncodedFormEntity(parameters);
            post.setEntity(entity);

            HttpResponse response = client.execute(post, localContext);
            if (response.getStatusLine().getStatusCode() == 302) {
                this.isLoggedIn = true;

                return new LoginResponse(true, null);
            } else { //if (response.getStatusLine().getStatusCode() == 200) {
                String html = EntityUtils.toString(response.getEntity());
                Document doc = Jsoup.parse(html, LOGIN_URL);
                Elements loggedIn = doc.getElementsByAttributeValue("id", "page-LOGIN");

                if (loggedIn.size() > 0) {
                    Elements errors = doc.getElementsByAttributeValue("id", "aerrors");
                    if (errors.size() > 0) {
                        Element error = errors.get(0);
                        return new LoginResponse(false, error.text());
                    } else {
                        return new LoginResponse(false, stringProvider.getString(R.string.login_failed));
                    }

                } else {
                    this.isLoggedIn = true;

                    return new LoginResponse(true, null);
                }
            }
        } catch (ConnectException e) {
            return new LoginResponse(false, stringProvider.getString(R.string.netflix_not_available));
        } catch (Exception e) {
            logger.error("", e);
            return new LoginResponse(false, stringProvider.getString(R.string.unexpected_error));
        }
    }

    private String getAuthUrl() throws ParseException, IOException, InterruptedException {
        Elements elements = null;
        String authUrl = null;

        //Netflix sometimes sends "BLOCKED", just try again
        int i = 0;
        while (i++ < MAX_AUTH_PAGE_RETRIES) {
            HttpContext localContext = new BasicHttpContext();
            localContext.setAttribute(ClientContext.COOKIE_STORE, cookieStore);
            HttpGet get = new HttpGet(LOGIN_URL);
            HttpResponse response = client.execute(get, localContext);
            String html = EntityUtils.toString(response.getEntity());

            Document doc = Jsoup.parse(html, LOGIN_URL);
            elements = doc.getElementsByAttributeValue("name", "authURL");
            if(elements != null && elements.size() > 0) {
                authUrl = elements.first().attr("value");
                break;
            } else {
                Thread.sleep(1000);
            }
        }

        return authUrl;
    }
}