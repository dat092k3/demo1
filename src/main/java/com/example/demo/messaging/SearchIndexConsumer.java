package com.example.demo.messaging;

import com.example.demo.config.RabbitMQConfig;
import com.example.demo.dto.message.SearchIndexMessage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Service;
import org.springframework.beans.factory.annotation.Autowired;
import com.example.demo.document.BookDocument;
import com.example.demo.document.ChapterDocument;
import com.example.demo.entity.Book;
import com.example.demo.entity.Chapter;
import com.example.demo.repository.BookRepository;
import com.example.demo.repository.BookSearchRepository;
import com.example.demo.repository.ChapterRepository;
import com.example.demo.repository.ChapterSearchRepository;

@Service
public class SearchIndexConsumer {

    private static final Logger logger = LoggerFactory.getLogger(SearchIndexConsumer.class);

    @Autowired
    private BookRepository bookRepository;

    @Autowired
    private ChapterRepository chapterRepository;

    @Autowired
    private BookSearchRepository bookSearchRepository;

    @Autowired
    private ChapterSearchRepository chapterSearchRepository;

    @RabbitListener(queues = RabbitMQConfig.SEARCH_INDEX_QUEUE)
    public void consumeSearchIndexMessage(SearchIndexMessage message) {
        logger.info("Received SearchIndexMessage: {} for {} with ID {}", message.getAction(), message.getEntityType(), message.getEntityId());
        
        try {
            if ("BOOK".equals(message.getEntityType())) {
                handleBookIndex(message);
            } else if ("CHAPTER".equals(message.getEntityType())) {
                handleChapterIndex(message);
            }
        } catch (Exception e) {
            logger.error("Error processing SearchIndexMessage", e);
        }
    }

    private void handleBookIndex(SearchIndexMessage message) {
        Long id = message.getEntityId();
        if ("DELETE".equals(message.getAction())) {
            bookSearchRepository.deleteById(id);
        } else {
            bookRepository.findById(id).ifPresent(book -> {
                String authorName = book.getAuthor() != null ? book.getAuthor().getName() : "";
                String categoryName = book.getCategory() != null ? book.getCategory().getName() : "";
                BookDocument bd = new BookDocument(book.getId(), book.getTitle(), book.getDescription(), authorName, categoryName);
                bookSearchRepository.save(bd);
            });
        }
    }

    private void handleChapterIndex(SearchIndexMessage message) {
        Long id = message.getEntityId();
        if ("DELETE".equals(message.getAction())) {
            chapterSearchRepository.deleteById(id);
        } else {
            chapterRepository.findById(id).ifPresent(chapter -> {
                String bookTitle = chapter.getBook() != null ? chapter.getBook().getTitle() : "";
                Long bookId = chapter.getBook() != null ? chapter.getBook().getId() : null;
                ChapterDocument cd = new ChapterDocument(chapter.getId(), chapter.getTitle(), chapter.getContent(), bookId, bookTitle);
                chapterSearchRepository.save(cd);
            });
        }
    }
}
