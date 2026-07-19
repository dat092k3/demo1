package com.example.demo.service;

import com.example.demo.document.BookDocument;
import com.example.demo.document.ChapterDocument;
import com.example.demo.entity.Book;
import com.example.demo.entity.Chapter;
import com.example.demo.repository.BookRepository;
import com.example.demo.repository.BookSearchRepository;
import com.example.demo.repository.ChapterRepository;
import com.example.demo.repository.ChapterSearchRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class DataMigrationService {

    private static final Logger logger = LoggerFactory.getLogger(DataMigrationService.class);

    @Autowired
    private BookRepository bookRepository;

    @Autowired
    private ChapterRepository chapterRepository;

    @Autowired
    private BookSearchRepository bookSearchRepository;

    @Autowired
    private ChapterSearchRepository chapterSearchRepository;

    public void reindexAll() {
        logger.info("Starting Elasticsearch reindex...");

        // Clear existing data
        bookSearchRepository.deleteAll();
        chapterSearchRepository.deleteAll();

        // Migrate Books
        List<Book> books = bookRepository.findAll();
        for (Book book : books) {
            String authorName = book.getAuthor() != null ? book.getAuthor().getName() : "";
            String categoryName = book.getCategory() != null ? book.getCategory().getName() : "";

            BookDocument bd = new BookDocument(book.getId(), book.getTitle(), book.getDescription(), authorName, categoryName);
            bookSearchRepository.save(bd);
        }
        logger.info("Migrated {} books to Elasticsearch", books.size());

        // Migrate Chapters
        List<Chapter> chapters = chapterRepository.findAll();
        for (Chapter chapter : chapters) {
            String bookTitle = chapter.getBook() != null ? chapter.getBook().getTitle() : "";
            Long bookId = chapter.getBook() != null ? chapter.getBook().getId() : null;

            ChapterDocument cd = new ChapterDocument(chapter.getId(), chapter.getTitle(), chapter.getContent(), bookId, bookTitle);
            chapterSearchRepository.save(cd);
        }
        logger.info("Migrated {} chapters to Elasticsearch", chapters.size());

        logger.info("Elasticsearch reindex completed.");
    }
}
