package com.example.demo.controller;

import com.example.demo.dto.BookDTO;
import com.example.demo.dto.ChapterDTO;
import com.example.demo.dto.ReadingProgressDTO;
import com.example.demo.dto.ReadingProgressRequestDTO;
import com.example.demo.service.ReadingService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/reading")
public class ReadingController {

    @Autowired
    private ReadingService readingService;

    // TODO: In a real app, userId should be extracted from the authenticated user (e.g., via SecurityContextHolder)
    // For this demonstration, we accept userId as a request parameter.
    
    // --- Favorite Books ---

    @PostMapping("/favorites/{bookId}")
    public ResponseEntity<Void> addFavoriteBook(@RequestParam Long userId, @PathVariable Long bookId) {
        readingService.addFavoriteBook(userId, bookId);
        return ResponseEntity.ok().build();
    }

    @DeleteMapping("/favorites/{bookId}")
    public ResponseEntity<Void> removeFavoriteBook(@RequestParam Long userId, @PathVariable Long bookId) {
        readingService.removeFavoriteBook(userId, bookId);
        return ResponseEntity.ok().build();
    }

    @GetMapping("/favorites")
    public ResponseEntity<List<BookDTO>> getFavoriteBooks(@RequestParam Long userId) {
        return ResponseEntity.ok(readingService.getFavoriteBooks(userId));
    }

    // --- Reading Progress ---

    @PostMapping("/progress")
    public ResponseEntity<Void> saveReadingProgress(@RequestParam Long userId, @Valid @RequestBody ReadingProgressRequestDTO request) {
        readingService.saveReadingProgress(userId, request);
        return ResponseEntity.ok().build();
    }

    @GetMapping("/progress/{bookId}")
    public ResponseEntity<ReadingProgressDTO> getReadingProgress(@RequestParam Long userId, @PathVariable Long bookId) {
        ReadingProgressDTO progress = readingService.getReadingProgress(userId, bookId);
        if (progress != null) {
            return ResponseEntity.ok(progress);
        }
        return ResponseEntity.notFound().build();
    }

    // --- Reading Book (Chapters) ---

    @GetMapping("/books/{bookId}/chapters")
    public ResponseEntity<List<ChapterDTO>> getBookChapters(@PathVariable Long bookId) {
        return ResponseEntity.ok(readingService.getBookChapters(bookId));
    }

    @GetMapping("/chapters/{chapterId}")
    public ResponseEntity<ChapterDTO> getChapterContent(@PathVariable Long chapterId) {
        return ResponseEntity.ok(readingService.getChapterContent(chapterId));
    }
}
