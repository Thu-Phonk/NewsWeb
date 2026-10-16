package com.example.news.util;

import com.mongodb.client.MongoCollection;
import com.mongodb.client.model.IndexOptions;
import com.mongodb.client.model.Indexes;
import org.bson.Document;

public final class ReactionCollection {
    private static volatile boolean indexesReady;

    private ReactionCollection() { }

    public static MongoCollection<Document> getCollection() {
        MongoCollection<Document> collection = MongoDBUtil.getDatabase().getCollection("reactions");
        if (!indexesReady) {
            synchronized (ReactionCollection.class) {
                if (!indexesReady) {
                    collection.createIndex(
                        Indexes.ascending("userId", "targetType", "targetId", "kind"),
                        new IndexOptions().unique(true).name("unique_user_reaction")
                    );
                    collection.createIndex(
                        Indexes.ascending("targetType", "targetId", "kind")
                    );
                    collection.createIndex(
                        Indexes.compoundIndex(
                            Indexes.ascending("userId", "kind"),
                            Indexes.descending("createdAt")
                        )
                    );
                    indexesReady = true;
                }
            }
        }
        return collection;
    }
}
