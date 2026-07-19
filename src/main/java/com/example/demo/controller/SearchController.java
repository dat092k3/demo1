package com.example.demo.controller;

import com.example.demo.document.BookDocument;
import com.example.demo.document.ChapterDocument;
import com.example.demo.dto.SearchResultDTO;
import com.example.demo.service.DataMigrationService;
import com.example.demo.service.SearchService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/search")
public class SearchController {

    @Autowired
    private SearchService searchService;

    @Autowired
    private DataMigrationService dataMigrationService;

    @GetMapping("/books")
    public ResponseEntity<SearchResultDTO<BookDocument>> searchBooks(
            @RequestParam(value = "q", required = false) String query,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        Pageable pageable = PageRequest.of(page, size);
        SearchResultDTO<BookDocument> result = searchService.searchBooks(query, pageable);
        return ResponseEntity.ok(result);
    }

    @GetMapping("/chapters")
    public ResponseEntity<SearchResultDTO<ChapterDocument>> searchChapters(
            @RequestParam(value = "q", required = false) String query,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        Pageable pageable = PageRequest.of(page, size);
        SearchResultDTO<ChapterDocument> result = searchService.searchChapters(query, pageable);
        return ResponseEntity.ok(result);
    }

    @PostMapping("/reindex")
    public ResponseEntity<String> reindexAll() {
        dataMigrationService.reindexAll();
        return ResponseEntity.ok("Reindexing completed successfully.");
    }
}
