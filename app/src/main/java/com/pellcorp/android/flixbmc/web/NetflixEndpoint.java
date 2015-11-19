package com.pellcorp.android.flixbmc.web;

/**
 * Created by jason on 18/11/15.
 */
public class NetflixEndpoint {
    public static final NetflixEndpoint DEFAULT = new NetflixEndpoint("www.netflix.com", true);

    private final String domain;
    private final boolean isTls;

    public NetflixEndpoint(final String domain, boolean isTls) {
        this.domain = domain;
        this.isTls = isTls;
    }

    public String getDomain() {
        return domain;
    }

    public String getHomePage() {
        if (isTls) {
            return "https://" + domain + "/";
        } else {
            return "http://" + domain + "/";
        }
    }
}
