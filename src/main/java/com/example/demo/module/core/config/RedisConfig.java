package com.example.demo.module.core.config;

import org.springframework.cache.annotation.EnableCaching;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.cache.RedisCacheConfiguration;
import org.springframework.data.redis.cache.RedisCacheManager;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.serializer.GenericJackson2JsonRedisSerializer;
import org.springframework.data.redis.serializer.RedisSerializationContext;
import org.springframework.data.redis.serializer.RedisSerializer;
import org.springframework.data.redis.serializer.StringRedisSerializer;
import org.springframework.data.redis.cache.RedisCacheWriter;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;

import java.time.Duration;
import java.util.HashMap;
import java.util.Map;

@Configuration
@EnableCaching
public class RedisConfig {

    // 1. Tạo một Serializer dùng chung đã cấu hình sẵn JavaTimeModule và Zstd
    // lại code
    private RedisSerializer<Object> createSerializer() {
        ObjectMapper objectMapper = new ObjectMapper();
        objectMapper.registerModule(new JavaTimeModule()); // Sửa lỗi LocalDate

        objectMapper.activateDefaultTyping(
                objectMapper.getPolymorphicTypeValidator(),
                ObjectMapper.DefaultTyping.NON_FINAL);

        GenericJackson2JsonRedisSerializer jsonSerializer = new GenericJackson2JsonRedisSerializer(objectMapper);
        return new ZstdRedisSerializer<>(jsonSerializer);
    }

    @Bean
    public RedisCacheManager cacheManager(RedisConnectionFactory connectionFactory) {
        RedisSerializer<Object> serializer = createSerializer();

        // Cấu hình mặc định (60 phút) sử dụng đúng Serializer đã sửa lỗi
        RedisCacheConfiguration defaultConfig = RedisCacheConfiguration.defaultCacheConfig()
                .computePrefixWith(cacheName -> "v2::" + cacheName + "::") // BẮT BUỘC ĐỂ TRÁNH LỖI CLASSNOTFOUND
                .entryTtl(Duration.ofMinutes(60))
                .serializeKeysWith(
                        RedisSerializationContext.SerializationPair.fromSerializer(new StringRedisSerializer()))
                .serializeValuesWith(
                        RedisSerializationContext.SerializationPair.fromSerializer(serializer));

        // Cấu hình thời gian lưu trữ (TTL) riêng biệt cho từng nghiệp vụ
        Map<String, RedisCacheConfiguration> cacheConfigurations = new HashMap<>();

        // Danh sách truyện (Ít thay đổi) -> 10 phút
        cacheConfigurations.put("books", defaultConfig.entryTtl(Duration.ofMinutes(10)));
        // Chi tiết một bộ truyện -> 30 phút
        cacheConfigurations.put("book", defaultConfig.entryTtl(Duration.ofMinutes(30)));
        // Nội dung chương chữ (Dữ liệu cực nặng, phân tán) -> Nâng lên 60 phút hoặc giữ
        // 15 phút tùy dung lượng RAM
        cacheConfigurations.put("chapters", defaultConfig.entryTtl(Duration.ofMinutes(15)));

        RedisCacheWriter cacheWriter = new JitterRedisCacheWriter(
                RedisCacheWriter.nonLockingRedisCacheWriter(connectionFactory));

        return RedisCacheManager.builder(cacheWriter)
                .cacheDefaults(defaultConfig)
                .withInitialCacheConfigurations(cacheConfigurations)
                .build();
    }

    @Bean
    public RedisTemplate<String, Object> redisTemplate(RedisConnectionFactory connectionFactory) {
        RedisTemplate<String, Object> template = new RedisTemplate<>();
        template.setConnectionFactory(connectionFactory);

        RedisSerializer<Object> serializer = createSerializer();

        // Sử dụng String cho Key
        template.setKeySerializer(new StringRedisSerializer());
        template.setHashKeySerializer(new StringRedisSerializer());

        // Value (Rất quan trọng cho luồng ghi đếm
        // view/thả tim)
        template.setValueSerializer(serializer);
        template.setHashValueSerializer(serializer);

        return template;
    }
}