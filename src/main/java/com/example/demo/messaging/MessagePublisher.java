package com.example.demo.messaging;

import com.example.demo.config.RabbitMQConfig;
import com.example.demo.dto.message.ReadingProgressMessage;
import com.example.demo.dto.message.SearchIndexMessage;
import com.example.demo.dto.message.ViewCountMessage;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class MessagePublisher {

    @Autowired
    private RabbitTemplate rabbitTemplate;

    public void publishViewCount(ViewCountMessage message) {
        rabbitTemplate.convertAndSend(RabbitMQConfig.EXCHANGE_NAME, RabbitMQConfig.VIEW_COUNT_ROUTING_KEY, message);
    }

    public void publishReadingProgress(ReadingProgressMessage message) {
        rabbitTemplate.convertAndSend(RabbitMQConfig.EXCHANGE_NAME, RabbitMQConfig.READING_PROGRESS_ROUTING_KEY, message);
    }

    public void publishSearchIndex(SearchIndexMessage message) {
        rabbitTemplate.convertAndSend(RabbitMQConfig.EXCHANGE_NAME, RabbitMQConfig.SEARCH_INDEX_ROUTING_KEY, message);
    }
}
