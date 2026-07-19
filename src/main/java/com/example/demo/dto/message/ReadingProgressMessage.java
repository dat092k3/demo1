package com.example.demo.dto.message;

import java.io.Serializable;
import java.time.LocalDateTime;

public class ReadingProgressMessage implements Serializable {
    private Long userId;
    private Long bookId;
    private Long chapterId;
    private LocalDateTime timestamp;

    public ReadingProgressMessage() {
    }

    public ReadingProgressMessage(Long userId, Long bookId, Long chapterId, LocalDateTime timestamp) {
        this.userId = userId;
        this.bookId = bookId;
        this.chapterId = chapterId;
        this.timestamp = timestamp;
    }

    public Long getUserId() {
        return userId;
    }

    public void setUserId(Long userId) {
        this.userId = userId;
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

    public LocalDateTime getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(LocalDateTime timestamp) {
        this.timestamp = timestamp;
    }
}
