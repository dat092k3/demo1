package com.example.demo.module.search.repository;

import com.example.demo.module.search.document.ChapterDocument;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.elasticsearch.repository.ElasticsearchRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ChapterSearchRepository extends ElasticsearchRepository<ChapterDocument, Long> {
    @org.springframework.data.elasticsearch.annotations.Query("{\"multi_match\": {\"query\": \"?0\", \"fields\": [\"title\", \"content\"]}}")
    Page<ChapterDocument> searchChapters(String keyword, Pageable pageable);
}
