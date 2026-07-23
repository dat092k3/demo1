package com.example.demo.module.catalog.entity;

import java.io.Serializable;
import java.util.Objects;

public class BookCategoryId implements Serializable {
    private Long book;
    private Long category;

    public BookCategoryId() {
    }

    public BookCategoryId(Long book, Long category) {
        this.book = book;
        this.category = category;
    }

    public Long getBook() {
        return book;
    }

    public void setBook(Long book) {
        this.book = book;
    }

    public Long getCategory() {
        return category;
    }

    public void setCategory(Long category) {
        this.category = category;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        BookCategoryId that = (BookCategoryId) o;
        return Objects.equals(book, that.book) && Objects.equals(category, that.category);
    }

    @Override
    public int hashCode() {
        return Objects.hash(book, category);
    }
}

