package com.url.shortner.repository;

import com.url.shortner.entity.UrlEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface UrlRepository extends JpaRepository<UrlEntity, Long> {

    UrlEntity findByShortUrl(String shortUrl);
}