package com.example.demo.entity;

import jakarta.persistence.*;
import java.io.Serializable;

@Entity
@Table(name = "book_categories")
@IdClass(BookCategoryId.class)
public class BookCategory {
    @Id
    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "book_id", nullable = false)
    private Book book;

    @Id
    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "category_id", nullable = false)
    private Category category;

    public BookCategory() {
    }

    public BookCategory(Book book, Category category) {
        this.book = book;
        this.category = category;
    }

    public Book getBook() {
        return book;
    }

    public void setBook(Book book) {
        this.book = book;
    }

    public Category getCategory() {
        return category;
    }

    public void setCategory(Category category) {
        this.category = category;
    }

    @Override
    public String toString() {
        return "BookCategory{" +
                "book=" + book.getId() +
                ", category=" + category.getId() +
                '}';
    }
}
