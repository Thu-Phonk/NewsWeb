package com.example.news.util;

import com.mongodb.client.model.Filters;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;
import org.bson.Document;
import org.bson.types.ObjectId;
import java.util.UUID;

public final class ReactionUtil {
    public static final String CSRF_KEY = "reactionCsrfToken";

    private ReactionUtil() { }

    public static boolean validType(String type) {
        return "editorial".equals(type) || "blog".equals(type);
    }

    public static Document publishedTarget(String type, ObjectId id) {
        if (!validType(type) || id == null) return null;
        var filter = Filters.and(Filters.eq("_id", id), Filters.eq("status", "published"));
        return "editorial".equals(type)
            ? EditorialCollection.getCollection().find(filter).first()
            : BlogCollection.getCollection().find(filter).first();
    }

    public static void prepare(HttpServletRequest request, String type, ObjectId id) {
        if (!validType(type) || id == null) throw new IllegalArgumentException("Invalid target");

        var collection = ReactionCollection.getCollection();
        long likes = collection.countDocuments(Filters.and(
            Filters.eq("targetType", type), Filters.eq("targetId", id), Filters.eq("kind", "like")
        ));
        Document user = RoleUtil.getCurrentUser(request);
        boolean liked = false;
        boolean bookmarked = false;
        if (user != null) {
            ObjectId userId = user.getObjectId("_id");
            liked = collection.find(Filters.and(
                Filters.eq("userId", userId), Filters.eq("targetType", type),
                Filters.eq("targetId", id), Filters.eq("kind", "like")
            )).first() != null;
            bookmarked = collection.find(Filters.and(
                Filters.eq("userId", userId), Filters.eq("targetType", type),
                Filters.eq("targetId", id), Filters.eq("kind", "bookmark")
            )).first() != null;
            HttpSession session = request.getSession(false);
            if (session != null && session.getAttribute(CSRF_KEY) == null) {
                session.setAttribute(CSRF_KEY, UUID.randomUUID().toString());
            }
        }
        request.setAttribute("reactionTargetType", type);
        request.setAttribute("reactionTargetId", id.toHexString());
        request.setAttribute("reactionLikeCount", likes);
        request.setAttribute("reactionLiked", liked);
        request.setAttribute("reactionBookmarked", bookmarked);
        request.setAttribute("reactionUser", user);
    }
}
