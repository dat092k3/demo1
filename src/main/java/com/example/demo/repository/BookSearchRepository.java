package com.example.demo.repository;

import com.example.demo.document.BookDocument;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.elasticsearch.repository.ElasticsearchRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface BookSearchRepository extends ElasticsearchRepository<BookDocument, Long> {
    @org.springframework.data.elasticsearch.annotations.Query("{\"multi_match\": {\"query\": \"?0\", \"fields\": [\"title\", \"description\", \"authorName\", \"categoryName\"]}}")
    Page<BookDocument> searchBooks(String keyword, Pageable pageable);
}
