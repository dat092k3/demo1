package com.example.demo.config;

import com.github.luben.zstd.Zstd;
import org.springframework.data.redis.serializer.RedisSerializer;
import org.springframework.data.redis.serializer.SerializationException;

public class ZstdRedisSerializer<T> implements RedisSerializer<T> {

    private final RedisSerializer<T> innerSerializer;

    public ZstdRedisSerializer(RedisSerializer<T> innerSerializer) {
        this.innerSerializer = innerSerializer;
    }

    @Override
    public byte[] serialize(T t) throws SerializationException {
        if (t == null) {
            return new byte[0];
        }
        try {
            byte[] bytes = innerSerializer.serialize(t);
            if (bytes == null || bytes.length == 0) {
                return bytes;
            }
            return Zstd.compress(bytes);
        } catch (Exception e) {
            throw new SerializationException("Could not serialize and compress data", e);
        }
    }

    @Override
    public T deserialize(byte[] bytes) throws SerializationException {
        if (bytes == null || bytes.length == 0) {
            return null;
        }
        try {
            long size = Zstd.decompressedSize(bytes);
            if (size <= 0) {
                return innerSerializer.deserialize(bytes);
            }
            byte[] decompressed = Zstd.decompress(bytes, (int) size);
            return innerSerializer.deserialize(decompressed);
        } catch (Exception e) {
            try {
                return innerSerializer.deserialize(bytes);
            } catch (Exception fallbackEx) {
                throw new SerializationException("Could not decompress and deserialize data", e);
            }
        }
    }
}
