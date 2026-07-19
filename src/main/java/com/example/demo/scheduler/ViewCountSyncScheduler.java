package com.example.demo.scheduler;

import com.example.demo.messaging.ViewCountConsumer;
import com.example.demo.repository.BookRepository;
import com.example.demo.repository.ChapterRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.util.Set;

@Component
@EnableScheduling
public class ViewCountSyncScheduler {

    private static final Logger logger = LoggerFactory.getLogger(ViewCountSyncScheduler.class);

    @Autowired
    private RedisTemplate<String, Object> redisTemplate;

    @Autowired
    private BookRepository bookRepository;

    @Autowired
    private ChapterRepository chapterRepository;

    @Scheduled(fixedRate = 300000) // 5 minutes
    public void syncViewCounts() {
        logger.info("Starting view count sync from Redis to MySQL...");

        // Sync Book View Counts
        Set<String> bookKeys = redisTemplate.keys(ViewCountConsumer.VIEW_COUNT_BOOK_PREFIX + "*");
        if (bookKeys != null) {
            for (String key : bookKeys) {
                try {
                    Long bookId = Long.parseLong(key.replace(ViewCountConsumer.VIEW_COUNT_BOOK_PREFIX, ""));
                    Integer viewCount = (Integer) redisTemplate.opsForValue().get(key);
                    if (viewCount != null && viewCount > 0) {
                        bookRepository.findById(bookId).ifPresent(book -> {
                            book.setViewCount(book.getViewCount() + viewCount);
                            bookRepository.save(book);
                        });
                        // Reset counter after sync
                        redisTemplate.delete(key);
                    }
                } catch (Exception e) {
                    logger.error("Error syncing view count for key {}: {}", key, e.getMessage());
                }
            }
        }

        logger.info("Finished view count sync.");
    }
}
