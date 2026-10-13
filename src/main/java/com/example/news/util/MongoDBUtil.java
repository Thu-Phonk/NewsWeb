package com.example.news.util;

import com.mongodb.client.MongoClient;
import com.mongodb.client.MongoClients;
import com.mongodb.client.MongoDatabase;

public class MongoDBUtil {

    private static final String CONNECTION_STRING =
            System.getenv("MONGODB_URI");

    private static final String DATABASE_NAME = "news_project";

    private static final MongoClient CLIENT =
            MongoClients.create(CONNECTION_STRING);

    private static final MongoDatabase DATABASE =
            CLIENT.getDatabase(DATABASE_NAME);

    public static MongoDatabase getDatabase() {
        return DATABASE;
    }

    public static void close() {
        CLIENT.close();
    }
}