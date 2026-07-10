package com.url.shortner.controller;

import com.url.shortner.entity.UrlRequest;
import com.url.shortner.entity.UrlResponse;
import com.url.shortner.service.UrlService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.net.URI;

@RestController
public class UrlShortnerController {

    @Autowired
    private UrlService urlService;

    @PostMapping(value = "/v1/shorten", consumes = "application/json", produces = "application/json")
    public ResponseEntity<UrlResponse> shortenUrl(@RequestBody UrlRequest request){
        return urlService.generateShortUrl(request.getOriginalUrl());

    }

    @GetMapping("/{id}")
    public ResponseEntity<Void> redirectToOriginalUrl(@PathVariable String id) {
        String originalUrl = urlService.redirectToOriginalUrl(id);
        if(originalUrl != null) {
            return ResponseEntity.status(302).header("Location", originalUrl).build();
        } else {
            return ResponseEntity.notFound().build();
        }
    }
}
