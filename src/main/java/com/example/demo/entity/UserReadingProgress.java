package com.example.demo.entity;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "user_reading_progress")
@IdClass(UserReadingProgressId.class)
public class UserReadingProgress {
    @Id
    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Id
    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "book_id", nullable = false)
    private Book book;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "chapter_id", nullable = false)
    private Chapter lastReadChapter;

    @Column(name = "last_read_date")
    private LocalDateTime lastReadDate;

    public UserReadingProgress() {
        this.lastReadDate = LocalDateTime.now();
    }

    public UserReadingProgress(User user, Book book, Chapter lastReadChapter) {
        this.user = user;
        this.book = book;
        this.lastReadChapter = lastReadChapter;
        this.lastReadDate = LocalDateTime.now();
    }


    public User getUser() {
        return user;
    }

    public void setUser(User user) {
        this.user = user;
    }

    public Book getBook() {
        return book;
    }

    public void setBook(Book book) {
        this.book = book;
    }

    public Chapter getLastReadChapter() {
        return lastReadChapter;
    }

    public void setLastReadChapter(Chapter lastReadChapter) {
        this.lastReadChapter = lastReadChapter;
    }

    public LocalDateTime getLastReadDate() {
        return lastReadDate;
    }

    public void setLastReadDate(LocalDateTime lastReadDate) {
        this.lastReadDate = lastReadDate;
    }

    @Override
    public String toString() {
        return "UserReadingProgress{" +
                "user=" + user.getId() +
                ", book=" + book.getId() +
                ", lastReadChapter=" + lastReadChapter.getId() +
                ", lastReadDate=" + lastReadDate +
                '}';
    }
}
