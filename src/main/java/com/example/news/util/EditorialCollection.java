
package com.example.news.util;

import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;
import org.bson.Document;

public final class EditorialCollection {

    private static final MongoDatabase DATABASE =
            MongoDBUtil.getDatabase();

    private static final MongoCollection<Document> COLLECTION =
            DATABASE.getCollection("editorial_articles");

    private EditorialCollection() {
    }

    public static MongoCollection<Document> getCollection() {
        return COLLECTION;
    }
}
