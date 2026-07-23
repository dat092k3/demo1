package com.example.demo.module.core.messaging;

import com.example.demo.module.core.config.RabbitMQConfig;
import com.example.demo.module.core.dto.message.ViewCountMessage;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

@Service
public class ViewCountConsumer {

    @Autowired
    private RedisTemplate<String, Object> redisTemplate;

    public static final String VIEW_COUNT_BOOK_PREFIX = "view:count:book:";
    public static final String VIEW_COUNT_CHAPTER_PREFIX = "view:count:chapter:";

    @RabbitListener(queues = RabbitMQConfig.VIEW_COUNT_QUEUE)
    public void consumeViewCountMessage(ViewCountMessage message) {
        if (message.getBookId() != null) {
            String bookKey = VIEW_COUNT_BOOK_PREFIX + message.getBookId();
            redisTemplate.opsForValue().increment(bookKey);
        }
    }
}
