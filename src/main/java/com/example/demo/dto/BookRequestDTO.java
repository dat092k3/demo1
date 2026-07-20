package com.example.demo.dto;

import jakarta.validation.constraints.*;
import java.time.LocalDate;
import org.springframework.lang.NonNull;

public class BookRequestDTO {
    @NotBlank(message = "Book title cannot be blank")
    @Size(min = 1, max = 200, message = "Book title must be between 1 and 200 characters")
    private String title;

    private String description;

    @PastOrPresent(message = "Published date cannot be in the future")
    private LocalDate publishedDate;

    @NotNull(message = "Author ID cannot be null")
    private Long authorId;

    @NotNull(message = "Category ID cannot be null")
    private Long categoryId;

    private Boolean isPublic;

    private String coverImage;

    private com.example.demo.enums.BookStatus status;

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

    @NonNull
    public Long getAuthorId() {
        return authorId;
    }

    public void setAuthorId(Long authorId) {
        this.authorId = authorId;
    }

    @NonNull
    public Long getCategoryId() {
        return categoryId;
    }

    public void setCategoryId(Long categoryId) {
        this.categoryId = categoryId;
    }

    public Boolean getIsPublic() {
        return isPublic;
    }

    public void setIsPublic(Boolean isPublic) {
        this.isPublic = isPublic;
    }

    public String getCoverImage() {
        return coverImage;
    }

    public void setCoverImage(String coverImage) {
        this.coverImage = coverImage;
    }

    public com.example.demo.enums.BookStatus getStatus() {
        return status;
    }

    public void setStatus(com.example.demo.enums.BookStatus status) {
        this.status = status;
    }
}
