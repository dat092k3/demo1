package com.example.demo.module.reading.dto;

import jakarta.validation.constraints.NotNull;

public class ReadingProgressRequestDTO {
    @NotNull(message = "Book ID cannot be null")
    private Long bookId;

    @NotNull(message = "Chapter ID cannot be null")
    private Long chapterId;

    public Long getBookId() {
        return bookId;
    }

    public void setBookId(Long bookId) {
        this.bookId = bookId;
    }

    public Long getChapterId() {
        return chapterId;
    }

    public void setChapterId(Long chapterId) {
        this.chapterId = chapterId;
    }
}
