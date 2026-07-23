package com.example.demo.service;

import com.example.demo.document.BookDocument;
import com.example.demo.document.ChapterDocument;
import com.example.demo.dto.SearchResultDTO;
import com.example.demo.repository.BookSearchRepository;
import com.example.demo.repository.ChapterSearchRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

@Service
public class SearchService {

    @Autowired
    private BookSearchRepository bookSearchRepository;

    @Autowired
    private ChapterSearchRepository chapterSearchRepository;

    public SearchResultDTO<BookDocument> searchBooks(String keyword, Pageable pageable) {
        Page<BookDocument> page;
        if (keyword == null || keyword.trim().isEmpty()) {
            page = bookSearchRepository.findAll(pageable);
        } else {
            page = bookSearchRepository.searchBooks(keyword, pageable);
        }
        return new SearchResultDTO<>(
                page.getContent(),
                page.getTotalElements(),
                page.getTotalPages(),
                page.getNumber()
        );
    }

    public SearchResultDTO<ChapterDocument> searchChapters(String keyword, Pageable pageable) {
        Page<ChapterDocument> page;
        if (keyword == null || keyword.trim().isEmpty()) {
            page = chapterSearchRepository.findAll(pageable);
        } else {
            page = chapterSearchRepository.searchChapters(keyword, pageable);
        }
        return new SearchResultDTO<>(
                page.getContent(),
                page.getTotalElements(),
                page.getTotalPages(),
                page.getNumber()
        );
    }
}
