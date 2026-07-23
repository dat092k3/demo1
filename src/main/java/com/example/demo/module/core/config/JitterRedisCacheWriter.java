package com.example.demo.module.core.config;

import org.springframework.data.redis.cache.RedisCacheWriter;
import org.springframework.data.redis.cache.CacheStatisticsCollector;
import org.springframework.data.redis.cache.CacheStatistics;
import org.springframework.lang.Nullable;

import java.time.Duration;
import java.util.concurrent.ThreadLocalRandom;
import java.util.concurrent.CompletableFuture;

public class JitterRedisCacheWriter implements RedisCacheWriter {

    private final RedisCacheWriter delegate;

    public JitterRedisCacheWriter(RedisCacheWriter delegate) {
        this.delegate = delegate;
    }

    private Duration addJitter(@Nullable Duration ttl) {
        if (ttl == null || ttl.isZero() || ttl.isNegative()) {
            return ttl;
        }
        long jitterSeconds = ThreadLocalRandom.current().nextLong(60, 301); // 1 to 5 minutes
        return ttl.plusSeconds(jitterSeconds);
    }

    @Override
    public void put(String name, byte[] key, byte[] value, @Nullable Duration ttl) {
        delegate.put(name, key, value, addJitter(ttl));
    }

    @Override
    public byte[] get(String name, byte[] key) {
        return delegate.get(name, key);
    }

    @Override
    public byte[] putIfAbsent(String name, byte[] key, byte[] value, @Nullable Duration ttl) {
        return delegate.putIfAbsent(name, key, value, addJitter(ttl));
    }

    @Override
    public void remove(String name, byte[] key) {
        delegate.remove(name, key);
    }

    @Override
    public void clean(String name, byte[] pattern) {
        delegate.clean(name, pattern);
    }

    @Override
    public void clearStatistics(String name) {
        delegate.clearStatistics(name);
    }

    @Override
    public RedisCacheWriter withStatisticsCollector(CacheStatisticsCollector collector) {
        return new JitterRedisCacheWriter(delegate.withStatisticsCollector(collector));
    }

    @Override
    public CompletableFuture<Void> store(String name, byte[] key, byte[] value, @Nullable Duration ttl) {
        return delegate.store(name, key, value, addJitter(ttl));
    }

    @Override
    public CompletableFuture<byte[]> retrieve(String name, byte[] key) {
        return delegate.retrieve(name, key);
    }

    @Override
    public CacheStatistics getCacheStatistics(String name) {
        return delegate.getCacheStatistics(name);
    }

    @Override
    public CompletableFuture<byte[]> retrieve(String name, byte[] key, @Nullable Duration ttl) {
        return delegate.retrieve(name, key, addJitter(ttl));
    }
}
