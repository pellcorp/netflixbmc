package com.pellcorp.android.flixbmc.web;

public class NetflixUrl {
    private static String NETFLIX_DOMAIN = "www.netflix.com/";

    private String resource;

    public NetflixUrl(String url) {
        this.resource = getResource(url);
    }

    public boolean isNetflixUrl() {
        return resource != null;
    }

    public boolean isWatch() {
        return resource != null && resource.startsWith("watch/");
    }

    public boolean isTitle() {
        return resource != null && resource.startsWith("title/");
    }

    public boolean isBrowse() {
        return resource != null && resource.startsWith("browse");
    }

    public boolean isDefault() {
        return resource != null && resource.equals("");
    }

    public String getId() {
        if (resource != null) {
            int indexOfSlash = resource.indexOf("/");
            if (indexOfSlash != -1) {
                return resource.substring(indexOfSlash + 1);
            }
        }

        //else
        return null;
    }

    private String getResource(String url) {
        int indexOf = url.indexOf(NETFLIX_DOMAIN);
        if (indexOf != -1) {
            url = url.substring(indexOf + NETFLIX_DOMAIN.length());
            int indexOfQuestion = url.indexOf("?");
            if (indexOfQuestion != -1) {
                url = url.substring(0, indexOfQuestion);
            }
            return url;
        } else {
            return null;
        }
    }
}
