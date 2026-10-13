
package com.example.news.util;

import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;
import org.bson.Document;

public final class BlogCollection {

    private static final MongoDatabase DATABASE =
            MongoDBUtil.getDatabase();

    private static final MongoCollection<Document> COLLECTION =
            DATABASE.getCollection("blogs");

    private BlogCollection() {
    }

    public static MongoCollection<Document> getCollection() {
        return COLLECTION;
    }
}
