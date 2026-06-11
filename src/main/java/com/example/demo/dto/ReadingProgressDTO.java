package com.example.demo.dto;

import java.time.LocalDateTime;

public class ReadingProgressDTO {
    private Long bookId;
    private String bookTitle;
    private Long lastReadChapterId;
    private String lastReadChapterTitle;
    private Integer lastReadPageNumber;
    private LocalDateTime lastReadDate;

    public Long getBookId() {
        return bookId;
    }

    public void setBookId(Long bookId) {
        this.bookId = bookId;
    }

    public String getBookTitle() {
        return bookTitle;
    }

    public void setBookTitle(String bookTitle) {
        this.bookTitle = bookTitle;
    }

    public Long getLastReadChapterId() {
        return lastReadChapterId;
    }

    public void setLastReadChapterId(Long lastReadChapterId) {
        this.lastReadChapterId = lastReadChapterId;
    }

    public String getLastReadChapterTitle() {
        return lastReadChapterTitle;
    }

    public void setLastReadChapterTitle(String lastReadChapterTitle) {
        this.lastReadChapterTitle = lastReadChapterTitle;
    }

    public Integer getLastReadPageNumber() {
        return lastReadPageNumber;
    }

    public void setLastReadPageNumber(Integer lastReadPageNumber) {
        this.lastReadPageNumber = lastReadPageNumber;
    }

    public LocalDateTime getLastReadDate() {
        return lastReadDate;
    }

    public void setLastReadDate(LocalDateTime lastReadDate) {
        this.lastReadDate = lastReadDate;
    }
}
