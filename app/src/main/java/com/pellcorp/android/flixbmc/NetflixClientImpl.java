package com.pellcorp.android.flixbmc;

import android.webkit.CookieManager;
import android.webkit.CookieSyncManager;
import android.webkit.WebResourceResponse;

import org.apache.http.Header;
import org.apache.http.HttpResponse;
import org.apache.http.HttpRequest;
import org.apache.http.HttpStatus;
import org.apache.http.NameValuePair;
import org.apache.http.ParseException;
import org.apache.http.ProtocolException;
import org.apache.http.client.CookieStore;
import org.apache.http.client.RedirectHandler;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpHead;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.params.AllClientPNames;
import org.apache.http.client.protocol.ClientContext;
import org.apache.http.cookie.Cookie;
import org.apache.http.impl.client.BasicCookieStore;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.impl.client.DefaultRedirectHandler;
import org.apache.http.impl.client.RedirectLocations;
import org.apache.http.impl.cookie.BasicClientCookie;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.params.BasicHttpParams;
import org.apache.http.params.HttpParams;
import org.apache.http.protocol.BasicHttpContext;
import org.apache.http.protocol.ExecutionContext;
import org.apache.http.protocol.HttpContext;
import org.apache.http.util.EntityUtils;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

public class NetflixClientImpl implements NetflixClient {
    private final CookieSyncManager syncManager;
    private final CookieManager cookieManager;

    private final Logger logger = LoggerFactory.getLogger(getClass().getName());

    private static final String USER_AGENT = "Mozilla/5.0 (Linux; Android 4.4; Nexus 5 Build/_BuildID_) AppleWebKit/537.36 (KHTML, like Gecko) Version/4.0 Chrome/30.0.0.0 Mobile Safari/537.36";
    public static final String LOGIN_URL = "https://signup.netflix.com/Login";

    private DefaultHttpClient client;
    private final CookieStore cookieStore = new BasicCookieStore();

    public NetflixClientImpl(final CookieSyncManager syncManager) {
        this.syncManager = syncManager;
        this.cookieManager = CookieManager.getInstance();

        HttpParams params = new BasicHttpParams();
        params.setParameter(AllClientPNames.USER_AGENT, USER_AGENT);
        this.client = new DefaultHttpClient(params);
        this.client.setCookieStore(cookieStore);
    }

    @Override
    public CookieStore getCookieStore() {
        return cookieStore;
    }

    @Override
    public WebResourceResponse loadUrl(String url) {
        if (url.contains("www.netflix.com/")) {
            if (url.contains("www.netflix.com/watch") || url.contains("www.netflix.com/title")) {
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
            BasicClientCookie cookie = new BasicClientCookie("forceWebsite", "true");
            cookie.setDomain(".netflix.com");
            cookie.setPath("/");
            cookieStore.addCookie(cookie);

            localContext.setAttribute(ClientContext.COOKIE_STORE, cookieStore);

            HttpResponse httpResponse = client.execute(get, localContext);

            InputStream is = null;

            if (url.contains("www.netflix.com/title")) {
                String cookies = cookieManager.getCookie("www.netflix.com/title");
                String content = EntityUtils.toString(httpResponse.getEntity());

                is = new ByteArrayInputStream(content.getBytes("UTF-8"));
            } else {
                is = httpResponse.getEntity().getContent();
            }

            String contentType = "text/html";
            Header contentTypeHeader = httpResponse.getEntity().getContentType();
            if (contentTypeHeader != null) {
                contentType = contentTypeHeader.getValue();
            }

            String charSet = EntityUtils.getContentCharSet(httpResponse.getEntity());
            WebResourceResponse response = new WebResourceResponse(contentType, charSet, is);

            return response;
        } catch (Exception e) {
            logger.error("Failed to load url" + url, e);

            throw new NetflixClientException(e);
        }
    }

    @Override
    public LoginResponse login(String email, String password) {
        try {
            String authUrl = getAuthUrl();
            if (authUrl == null) {
                return new LoginResponse(false, "Login failed");
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
                return new LoginResponse(true, null);
            } else { //if (response.getStatusLine().getStatusCode() == 200) {
                String html = EntityUtils.toString(response.getEntity());
                Document doc = Jsoup.parse(html, LOGIN_URL);
                Elements loggedIn = doc.getElementsByAttributeValue("id", "page-LOGIN");

                if (loggedIn.size() > 0) {
                    //<div id="aerrors"><ul><li>Sorry, we are unable to process your request. Please try again later.</li></ul></div>
                    Elements errors = doc.getElementsByAttributeValue("id", "aerrors");
                    if (errors.size() > 0) {
                        Element error = errors.get(0);
                        return new LoginResponse(false, error.text());
                    } else {

                        return new LoginResponse(false, "Login failed");
                    }

                } else {
                    return new LoginResponse(true, null);
                }
            }
        } catch (Exception e) {
            logger.error("Failed to login", e);
            return new LoginResponse(false, e.getMessage());
        }
    }

    private String getAuthUrl() throws ParseException, IOException {
        HttpGet get = new HttpGet(LOGIN_URL);

        HttpContext localContext = new BasicHttpContext();

        HttpResponse response = client.execute(get, localContext);
        if (response.getStatusLine().getStatusCode() == 200) {
            String html = EntityUtils.toString(response.getEntity());
            if (html != null) {
                Document doc = Jsoup.parse(html, LOGIN_URL);
                Elements elements = doc.getElementsByAttributeValue("name", "authURL");
                String authUrl = elements.first().attr("value");
                return authUrl;
            } else {
                return null;
            }
        } else {
            return null;
        }
    }
}