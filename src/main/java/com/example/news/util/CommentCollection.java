
package com.example.news.util;

import com.mongodb.client.MongoCollection;
import org.bson.Document;

public final class CommentCollection {

    private CommentCollection() {
    }

    public static MongoCollection<Document> getCollection() {
        return MongoDBUtil.getDatabase()
                .getCollection("comments");
    }
}
