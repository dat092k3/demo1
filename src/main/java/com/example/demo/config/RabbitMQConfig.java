package com.example.demo.config;

import org.springframework.amqp.core.*;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.amqp.support.converter.MessageConverter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;

@Configuration
public class RabbitMQConfig {

    public static final String EXCHANGE_NAME = "demo1.exchange";

    public static final String VIEW_COUNT_QUEUE = "view.count.queue";
    public static final String READING_PROGRESS_QUEUE = "reading.progress.queue";
    public static final String SEARCH_INDEX_QUEUE = "search.index.queue";

    public static final String VIEW_COUNT_ROUTING_KEY = "view.count.routing.key";
    public static final String READING_PROGRESS_ROUTING_KEY = "reading.progress.routing.key";
    public static final String SEARCH_INDEX_ROUTING_KEY = "search.index.routing.key";

    @Bean
    public TopicExchange exchange() {
        return new TopicExchange(EXCHANGE_NAME);
    }

    @Bean
    public Queue viewCountQueue() {
        return new Queue(VIEW_COUNT_QUEUE, true);
    }

    @Bean
    public Queue readingProgressQueue() {
        return new Queue(READING_PROGRESS_QUEUE, true);
    }

    @Bean
    public Queue searchIndexQueue() {
        return new Queue(SEARCH_INDEX_QUEUE, true);
    }

    @Bean
    public Binding viewCountBinding(Queue viewCountQueue, TopicExchange exchange) {
        return BindingBuilder.bind(viewCountQueue).to(exchange).with(VIEW_COUNT_ROUTING_KEY);
    }

    @Bean
    public Binding readingProgressBinding(Queue readingProgressQueue, TopicExchange exchange) {
        return BindingBuilder.bind(readingProgressQueue).to(exchange).with(READING_PROGRESS_ROUTING_KEY);
    }

    @Bean
    public Binding searchIndexBinding(Queue searchIndexQueue, TopicExchange exchange) {
        return BindingBuilder.bind(searchIndexQueue).to(exchange).with(SEARCH_INDEX_ROUTING_KEY);
    }

    @Bean
    public MessageConverter jsonMessageConverter() {
        ObjectMapper objectMapper = new ObjectMapper();
        objectMapper.registerModule(new JavaTimeModule());
        return new Jackson2JsonMessageConverter(objectMapper);
    }

    @Bean
    public RabbitTemplate rabbitTemplate(ConnectionFactory connectionFactory) {
        final RabbitTemplate rabbitTemplate = new RabbitTemplate(connectionFactory);
        rabbitTemplate.setMessageConverter(jsonMessageConverter());
        return rabbitTemplate;
    }
}
