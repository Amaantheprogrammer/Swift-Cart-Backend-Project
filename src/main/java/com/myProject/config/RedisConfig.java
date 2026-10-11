package com.myProject.config;

import java.time.Duration;

import org.springframework.cache.annotation.EnableCaching;
import org.springframework.context.annotation.*;
import org.springframework.data.redis.cache.*;
import org.springframework.data.redis.connection.RedisConnectionFactory;

@Configuration
@EnableCaching
public class RedisConfig {
    @Bean 
    public RedisCacheManager cacheManager(RedisConnectionFactory connectionFactory) {
        return RedisCacheManager.builder(connectionFactory)
                .withCacheConfiguration(
                    "products", 
                    RedisCacheConfiguration.defaultCacheConfig().entryTtl(Duration.ofMinutes(30))
                )
                .withCacheConfiguration(
                    "productsById", 
                    RedisCacheConfiguration.defaultCacheConfig().entryTtl(Duration.ofMinutes(30))
                )
                .withCacheConfiguration(
                    "productsByNameContainingIgnoreCaseAndPriceLessThan", 
                    RedisCacheConfiguration.defaultCacheConfig().entryTtl(Duration.ofMinutes(30))
                )
                .withCacheConfiguration(
                    "productsByCategoryIdAndPriceLessThan", 
                    RedisCacheConfiguration.defaultCacheConfig().entryTtl(Duration.ofMinutes(30))
                )
                .withCacheConfiguration(
                    "categories",
                    RedisCacheConfiguration.defaultCacheConfig().entryTtl(Duration.ofHours(12))
                )
                .withCacheConfiguration(
                    "users",
                    RedisCacheConfiguration.defaultCacheConfig().entryTtl(Duration.ofMinutes(10))
                )
                .withCacheConfiguration(
                    "usersById",
                    RedisCacheConfiguration.defaultCacheConfig().entryTtl(Duration.ofMinutes(10))
                )
                .withCacheConfiguration(
                    "usersByEmail",
                    RedisCacheConfiguration.defaultCacheConfig().entryTtl(Duration.ofMinutes(10))
                )
                .build();
    }
}