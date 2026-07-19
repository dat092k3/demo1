package com.example.demo.dto.message;

import java.io.Serializable;
import java.time.LocalDateTime;

public class ViewCountMessage implements Serializable {
    private Long bookId;
    private Long chapterId;
    private Long userId; // can be null for anonymous
    private LocalDateTime timestamp;

    public ViewCountMessage() {
    }

    public ViewCountMessage(Long bookId, Long chapterId, Long userId, LocalDateTime timestamp) {
        this.bookId = bookId;
        this.chapterId = chapterId;
        this.userId = userId;
        this.timestamp = timestamp;
    }

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

    public Long getUserId() {
        return userId;
    }

    public void setUserId(Long userId) {
        this.userId = userId;
    }

    public LocalDateTime getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(LocalDateTime timestamp) {
        this.timestamp = timestamp;
    }
}
