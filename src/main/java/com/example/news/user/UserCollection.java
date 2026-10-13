
package com.example.news.user;

import com.mongodb.client.MongoClient;
import com.mongodb.client.MongoClients;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;
import org.bson.Document;

public final class UserCollection {

    private static final String DATABASE_NAME = "test";
    private static final String COLLECTION_NAME = "users";

    private static final MongoClient CLIENT;
    private static final MongoCollection<Document> USERS;

    static {
        String connectionString = System.getenv("MONGODB_URI");

        if (connectionString == null || connectionString.isBlank()) {
            throw new IllegalStateException(
                "MONGODB_URI environment variable is not configured."
            );
        }

        CLIENT = MongoClients.create(connectionString);

        MongoDatabase database = CLIENT.getDatabase(DATABASE_NAME);
        USERS = database.getCollection(COLLECTION_NAME);
    }

    private UserCollection() {
        // Utility class; do not instantiate.
    }

    public static MongoCollection<Document> getUsers() {
        return USERS;
    }
}
