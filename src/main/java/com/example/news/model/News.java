package com.example.news.model;

import org.bson.types.ObjectId;

import java.time.LocalDateTime;

public class News {

    private ObjectId id;
    private String title;
    private String content;
    private String image;
    private String author;
    private String category;
    private LocalDateTime createdAt;

    public News() {
    }

    public News(String title, String content, String image,
                String author, String category) {

        this.title = title;
        this.content = content;
        this.image = image;
        this.author = author;
        this.category = category;
        this.createdAt = LocalDateTime.now();
    }

    public ObjectId getId() {
        return id;
    }

    public void setId(ObjectId id) {
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

    public String getImage() {
        return image;
    }

    public void setImage(String image) {
        this.image = image;
    }

    public String getAuthor() {
        return author;
    }

    public void setAuthor(String author) {
        this.author = author;
    }

    public String getCategory() {
        return category;
    }

    public void setCategory(String category) {
        this.category = category;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }
}