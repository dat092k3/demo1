package com.example.demo.document;

import org.springframework.data.annotation.Id;
import org.springframework.data.elasticsearch.annotations.Document;
import org.springframework.data.elasticsearch.annotations.Field;
import org.springframework.data.elasticsearch.annotations.FieldType;

@Document(indexName = "books")
public class BookDocument {

    @Id
    private Long id;

    @Field(type = FieldType.Text, analyzer = "icu_analyzer")
    private String title;

    @Field(type = FieldType.Text, analyzer = "icu_analyzer")
    private String description;

    @Field(type = FieldType.Keyword)
    private String authorName;

    @Field(type = FieldType.Keyword)
    private String categoryName;

    @Field(type = FieldType.Keyword)
    private String status;

    public BookDocument() {
    }

    public BookDocument(Long id, String title, String description, String authorName, String categoryName, String status) {
        this.id = id;
        this.title = title;
        this.description = description;
        this.authorName = authorName;
        this.categoryName = categoryName;
        this.status = status;
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

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getAuthorName() {
        return authorName;
    }

    public void setAuthorName(String authorName) {
        this.authorName = authorName;
    }

    public String getCategoryName() {
        return categoryName;
    }

    public void setCategoryName(String categoryName) {
        this.categoryName = categoryName;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }
}
