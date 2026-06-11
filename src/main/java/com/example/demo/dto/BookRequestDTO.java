package com.example.demo.dto;

import jakarta.validation.constraints.*;
import java.time.LocalDate;

public class BookRequestDTO {
    @NotBlank(message = "Book title cannot be blank")
    @Size(min = 1, max = 200, message = "Book title must be between 1 and 200 characters")
    private String title;

    private String description;

    @PastOrPresent(message = "Published date cannot be in the future")
    private LocalDate publishedDate;

    @Positive(message = "Quantity must be greater than 0")
    @NotNull(message = "Quantity cannot be null")
    private Integer quantity;

    @NotNull(message = "Author ID cannot be null")
    private Long authorId;

    @NotNull(message = "Category ID cannot be null")
    private Long categoryId;

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public LocalDate getPublishedDate() {
        return publishedDate;
    }

    public void setPublishedDate(LocalDate publishedDate) {
        this.publishedDate = publishedDate;
    }

    public Integer getQuantity() {
        return quantity;
    }

    public void setQuantity(Integer quantity) {
        this.quantity = quantity;
    }

    public Long getAuthorId() {
        return authorId;
    }

    public void setAuthorId(Long authorId) {
        this.authorId = authorId;
    }

    public Long getCategoryId() {
        return categoryId;
    }

    public void setCategoryId(Long categoryId) {
        this.categoryId = categoryId;
    }
}
