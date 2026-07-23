package com.example.demo.module.reading.entity;

import java.io.Serializable;
import java.util.Objects;

public class UserFavoriteBookId implements Serializable {
    private Long user;
    private Long book;

    public UserFavoriteBookId() {
    }

    public UserFavoriteBookId(Long user, Long book) {
        this.user = user;
        this.book = book;
    }

    public Long getUser() {
        return user;
    }

    public void setUser(Long user) {
        this.user = user;
    }

    public Long getBook() {
        return book;
    }

    public void setBook(Long book) {
        this.book = book;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        UserFavoriteBookId that = (UserFavoriteBookId) o;
        return Objects.equals(user, that.user) && Objects.equals(book, that.book);
    }

    @Override
    public int hashCode() {
        return Objects.hash(user, book);
    }
}

