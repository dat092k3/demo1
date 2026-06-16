package com.example.demo.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.*;

@Entity
@Table(name = "chapters")
public class Chapter {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "chapter_id")
    private Long id;

    @NotBlank(message = "Chapter title cannot be blank")
    @Size(min = 1, max = 200, message = "Chapter title must be between 1 and 200 characters")
    @Pattern(regexp = "^[\\p{L}\\s'-:.,&()]+$",
            message = "Chapter title can only contain letters, numbers, spaces, and common punctuation marks")
    @Column(name = "title", nullable = false, length = 200)
    private String title;

    @Positive(message = "Page number must be greater than 0")
    @Column(name = "page_number")
    private Integer pageNumber;

    @Column(name = "content", columnDefinition = "LONGTEXT")
    private String content;

    @NotNull(message = "Book cannot be null")
    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "book_id", nullable = false)
    private Book book;

    @Column(name = "is_public", nullable = false)
    private boolean isPublic = false;

    public Chapter() {
    }

    public Chapter(String title, Integer pageNumber, String content, Book book) {
        this.title = title;
        this.pageNumber = pageNumber;
        this.content = content;
        this.book = book;
    }

    // ...existing code...
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public Integer getPageNumber() {
        return pageNumber;
    }

    public void setPageNumber(Integer pageNumber) {
        this.pageNumber = pageNumber;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public Book getBook() {
        return book;
    }

    public void setBook(Book book) {
        this.book = book;
    }

    public boolean isPublic() {
        return isPublic;
    }

    public void setPublic(boolean isPublic) {
        this.isPublic = isPublic;
    }

    @Override
    public String toString() {
        return "Chapter{" +
                "id=" + id +
                ", title='" + title + '\'' +
                ", pageNumber=" + pageNumber +
                '}';
    }
}
