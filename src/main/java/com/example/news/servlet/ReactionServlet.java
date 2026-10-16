package com.example.news.servlet;

import com.example.news.util.ReactionCollection;
import com.example.news.util.ReactionUtil;
import com.example.news.util.RoleUtil;
import com.mongodb.MongoWriteException;
import com.mongodb.client.model.Filters;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import org.bson.Document;
import org.bson.types.ObjectId;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.util.Date;

@WebServlet("/reaction")
public class ReactionServlet extends HttpServlet {
    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws IOException {
        request.setCharacterEncoding("UTF-8");
        response.setHeader("Cache-Control", "no-store");
        try {
            Document user = RoleUtil.getCurrentUser(request);
            if (user == null) { response.sendError(401, "Please log in."); return; }
            HttpSession session = request.getSession(false);
            if (session == null) { response.sendError(403, "Session expired."); return; }
            Object token = session.getAttribute(ReactionUtil.CSRF_KEY);
            String posted = request.getParameter("csrfToken");
            if (!(token instanceof String expected) || posted == null ||
                !MessageDigest.isEqual(expected.getBytes(StandardCharsets.UTF_8),
                    posted.getBytes(StandardCharsets.UTF_8))) {
                response.sendError(403, "Invalid form token."); return;
            }

            String type = request.getParameter("targetType");
            String idText = request.getParameter("targetId");
            String action = request.getParameter("action");
            if (!ReactionUtil.validType(type) || idText == null || !ObjectId.isValid(idText)) {
                response.sendError(400, "Invalid article or blog."); return;
            }
            if (!"like".equals(action) && !"unlike".equals(action) &&
                !"bookmark".equals(action) && !"unbookmark".equals(action)) {
                response.sendError(400, "Invalid reaction action."); return;
            }
            ObjectId targetId = new ObjectId(idText);
            boolean adding = "like".equals(action) || "bookmark".equals(action);
            String kind = ("like".equals(action) || "unlike".equals(action)) ? "like" : "bookmark";
            // Creation only on currently published material. Removal allowed even if later unpublished.
            if (adding && ReactionUtil.publishedTarget(type, targetId) == null) {
                response.sendError(404, "Published article or blog not found."); return;
            }
            ObjectId userId = user.getObjectId("_id");
            var collection = ReactionCollection.getCollection();
            var key = Filters.and(
                Filters.eq("userId", userId), Filters.eq("targetType", type),
                Filters.eq("targetId", targetId), Filters.eq("kind", kind)
            );
            if (adding) {
                Document reaction = new Document("userId", userId)
                    .append("targetType", type).append("targetId", targetId)
                    .append("kind", kind).append("createdAt", new Date());
                try {
                    collection.insertOne(reaction);
                } catch (MongoWriteException e) {
                    // Unique index makes repeated clicks idempotent; other database errors must surface.
                    if (e.getError() == null || e.getError().getCode() != 11000) throw e;
                }
            } else {
                collection.deleteOne(key);
            }
            session.removeAttribute(ReactionUtil.CSRF_KEY);
            String path = "editorial".equals(type) ? "/editorial?id=" : "/blog/view?id=";
            response.setStatus(HttpServletResponse.SC_SEE_OTHER);
            response.setHeader("Location", request.getContextPath() + path + targetId.toHexString() + "#reactions");
        } catch (Exception e) {
            getServletContext().log("Reaction update failed.", e);
            if (!response.isCommitted()) response.sendError(500, "Unable to update reaction.");
        }
    }
}
