package com.example.demo.controller;

import com.example.demo.dto.ChapterDTO;
import com.example.demo.dto.ChapterRequestDTO;
import com.example.demo.service.ChapterService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.http.MediaType;

@RestController
@RequestMapping("/api/chapters")
public class ChapterController {

    @Autowired
    private ChapterService chapterService;

    @PostMapping(value = "/add-to-book/{bookId}", consumes = { MediaType.MULTIPART_FORM_DATA_VALUE })
    public ResponseEntity<ChapterDTO> createChapter(
            @PathVariable Long bookId,
            @RequestPart("chapter") @Valid ChapterRequestDTO requestDTO,
            @RequestPart(value = "file", required = false) MultipartFile file) {
        return ResponseEntity.status(HttpStatus.CREATED).body(chapterService.createChapter(bookId, requestDTO, file));
    }

    @PutMapping(value = "/update/{chapterId}", consumes = { MediaType.MULTIPART_FORM_DATA_VALUE })
    public ResponseEntity<ChapterDTO> updateChapter(
            @PathVariable Long chapterId,
            @RequestPart("chapter") @Valid ChapterRequestDTO requestDTO,
            @RequestPart(value = "file", required = false) MultipartFile file) {
        return ResponseEntity.ok(chapterService.updateChapter(chapterId, requestDTO, file));
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
