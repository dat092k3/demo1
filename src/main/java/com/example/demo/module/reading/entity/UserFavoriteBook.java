package com.example.demo.module.reading.entity;

import com.example.demo.module.catalog.entity.Book;

import com.example.demo.module.iam.entity.User;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "user_favorite_books")
@IdClass(UserFavoriteBookId.class)
public class UserFavoriteBook {
    @Id
    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Id
    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "book_id", nullable = false)
    private Book book;

    @Column(name = "added_date")
    private LocalDateTime addedDate;

    public UserFavoriteBook() {
        this.addedDate = LocalDateTime.now();
    }

    public UserFavoriteBook(User user, Book book) {
        this.user = user;
        this.book = book;
        this.addedDate = LocalDateTime.now();
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

    public LocalDateTime getAddedDate() {
        return addedDate;
    }

    public void setAddedDate(LocalDateTime addedDate) {
        this.addedDate = addedDate;
    }

    @Override
    public String toString() {
        return "UserFavoriteBook{" +
                "user=" + user.getId() +
                ", book=" + book.getId() +
                ", addedDate=" + addedDate +
                '}';
    }
}
