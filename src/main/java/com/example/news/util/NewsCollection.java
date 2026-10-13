package com.example.news.util;

import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;
import org.bson.Document;

public class NewsCollection {

    private static final MongoDatabase DATABASE =
            MongoDBUtil.getDatabase();

    private static final MongoCollection<Document> COLLECTION =
            DATABASE.getCollection("news");

    public static MongoCollection<Document> getCollection() {
        return COLLECTION;
    }
}