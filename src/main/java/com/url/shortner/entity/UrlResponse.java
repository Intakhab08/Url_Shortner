package com.url.shortner.entity;

public class UrlResponse {

    private String shortUrl;

    public UrlResponse(String shortUrl){
        this.shortUrl = shortUrl;
    }

    public String getShortUrl(){
        return shortUrl;
    }

    public void setShortUrl(String shortUrl){
        this.shortUrl = shortUrl;
    }
}
