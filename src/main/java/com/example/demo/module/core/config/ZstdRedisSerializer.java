package com.example.demo.module.core.config;

import com.github.luben.zstd.Zstd;
import com.github.luben.zstd.ZstdInputStream;
import org.springframework.data.redis.serializer.RedisSerializer;
import org.springframework.data.redis.serializer.SerializationException;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;

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
        
        // Thử giải nén bằng ZstdInputStream để tránh dùng decompressedSize() (bị deprecated và có rủi ro bảo mật)
        try (ByteArrayInputStream bais = new ByteArrayInputStream(bytes);
             ZstdInputStream zis = new ZstdInputStream(bais);
             ByteArrayOutputStream baos = new ByteArrayOutputStream()) {

            byte[] buffer = new byte[8192];
            int read;
            while ((read = zis.read(buffer)) != -1) {
                baos.write(buffer, 0, read);
            }
            return innerSerializer.deserialize(baos.toByteArray());
        } catch (Exception e) {
            // Fallback: Nếu không giải nén được (có thể dữ liệu cũ chưa bị nén), thử đọc trực tiếp
            try {
                return innerSerializer.deserialize(bytes);
            } catch (Exception fallbackEx) {
                throw new SerializationException("Could not decompress and deserialize data", e);
            }
        }
    }
}
