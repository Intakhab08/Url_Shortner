package com.url.shortner.service;

import com.url.shortner.entity.UrlEntity;
import com.url.shortner.entity.UrlResponse;
import com.url.shortner.repository.UrlRepository;
import com.url.shortner.utility.Base62Encoder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

@Service
public class UrlService {

    @Autowired
    StringRedisTemplate redisTemplate;

    @Value("${cache.expiration.seconds}")
    private long redisExpirationTime;

    @Autowired
    UrlRepository urlRepository;

    public ResponseEntity<UrlResponse> generateShortUrl(String originalUrl) {
        if(originalUrl!=null && !originalUrl.isEmpty()){
            UrlEntity urlEntity = new UrlEntity();
            urlEntity.setOriginalUrl(originalUrl);
            UrlEntity urlEntityDB = urlRepository.save(urlEntity);
            String shortUrl = Base62Encoder.encode(urlEntityDB.getId());
            shortUrl = "http://localhost:8080/" + shortUrl;
            urlEntityDB.setShortUrl(shortUrl);
            urlRepository.save(urlEntityDB);
            UrlResponse response = new UrlResponse(shortUrl);
            return ResponseEntity.ok(response);
        } else {
            return ResponseEntity.badRequest().build();
        }
    }

    public String redirectToOriginalUrl(String id) {
        String shortUrl = "http://localhost:8080/" + id;
        String chachedValue = redisTemplate.opsForValue().get(shortUrl);
        if(chachedValue != null){
            return chachedValue;
        }
        UrlEntity urlEntity = urlRepository.findByShortUrl(shortUrl);
        if (urlEntity != null) {
            redisTemplate.opsForValue().set(shortUrl, urlEntity.getOriginalUrl(), redisExpirationTime);
            return urlEntity.getOriginalUrl();
        } else {
            return null;
        }
    }
}
