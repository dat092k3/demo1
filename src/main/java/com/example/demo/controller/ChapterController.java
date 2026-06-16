package com.example.demo.controller;

import com.example.demo.dto.ChapterDTO;
import com.example.demo.dto.ChapterRequestDTO;
import com.example.demo.service.ChapterService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/chapters")
public class ChapterController {

    @Autowired
    private ChapterService chapterService;

    @PostMapping("/add-to-book/{bookId}")
    public ResponseEntity<ChapterDTO> createChapter(
            @PathVariable Long bookId,
            @Valid @RequestBody ChapterRequestDTO requestDTO) {
        return ResponseEntity.status(HttpStatus.CREATED).body(chapterService.createChapter(bookId, requestDTO));
    }

    @PutMapping("/update/{chapterId}")
    public ResponseEntity<ChapterDTO> updateChapter(
            @PathVariable Long chapterId,
            @Valid @RequestBody ChapterRequestDTO requestDTO) {
        return ResponseEntity.ok(chapterService.updateChapter(chapterId, requestDTO));
    }

    @DeleteMapping("/delete/{chapterId}")
    public ResponseEntity<Void> deleteChapter(@PathVariable Long chapterId) {
        chapterService.deleteChapter(chapterId);
        return ResponseEntity.noContent().build();
    }

    @PatchMapping("/change-visibility/{chapterId}")
    public ResponseEntity<ChapterDTO> toggleChapterVisibility(
            @PathVariable Long chapterId,
            @RequestParam boolean isPublic) {
        return ResponseEntity.ok(chapterService.toggleChapterVisibility(chapterId, isPublic));
    }
}
