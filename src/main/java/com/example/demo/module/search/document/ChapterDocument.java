package com.example.demo.module.search.document;

import org.springframework.data.annotation.Id;
import org.springframework.data.elasticsearch.annotations.Document;
import org.springframework.data.elasticsearch.annotations.Field;
import org.springframework.data.elasticsearch.annotations.FieldType;

@Document(indexName = "chapters")
public class ChapterDocument {

    @Id
    private Long id;

    @Field(type = FieldType.Text, analyzer = "icu_analyzer")
    private String title;

    @Field(type = FieldType.Text, analyzer = "icu_analyzer")
    private String content;

    @Field(type = FieldType.Long)
    private Long bookId;

    @Field(type = FieldType.Keyword)
    private String bookTitle;

    public ChapterDocument() {
    }

    public ChapterDocument(Long id, String title, String content, Long bookId, String bookTitle) {
        this.id = id;
        this.title = title;
        this.content = content;
        this.bookId = bookId;
        this.bookTitle = bookTitle;
    }

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

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public Long getBookId() {
        return bookId;
    }

    public void setBookId(Long bookId) {
        this.bookId = bookId;
    }

    public String getBookTitle() {
        return bookTitle;
    }

    public void setBookTitle(String bookTitle) {
        this.bookTitle = bookTitle;
    }
}
