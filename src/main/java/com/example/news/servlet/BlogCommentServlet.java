package com.example.news.servlet;

import com.example.news.util.BlogCollection;
import com.example.news.util.CommentCollection;
import com.example.news.util.RoleUtil;
import com.mongodb.client.model.Filters;
import org.bson.Document;
import org.bson.types.ObjectId;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.util.Date;

@WebServlet("/blog/comment")
public class BlogCommentServlet extends HttpServlet {
    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws IOException {
        request.setCharacterEncoding("UTF-8");
        response.setHeader("Cache-Control", "no-store");
        try {
            Document user = RoleUtil.getCurrentUser(request);
            if (user == null) {
                response.sendError(401, "Login required.");
                return;
            }
            HttpSession session = request.getSession(false);
            if (session == null || !validToken(session.getAttribute("blogCommentCsrfToken"),
                    request.getParameter("csrfToken"))) {
                response.sendError(403, "Invalid comment form token.");
                return;
            }
            String id = request.getParameter("blogId");
            if (id == null || !ObjectId.isValid(id)) {
                response.sendError(400, "Invalid blog ID.");
                return;
            }
            ObjectId blogId = new ObjectId(id);
            if (BlogCollection.getCollection().find(Filters.and(
                    Filters.eq("_id", blogId), Filters.eq("status", "published"))).first() == null) {
                response.sendError(404, "Published blog not found.");
                return;
            }
            String body = request.getParameter("body");
            body = body == null ? "" : body.trim();
            if (body.length() < 2 || body.length() > 2000) {
                response.sendError(400, "Comment must be 2-2000 characters.");
                return;
            }
            ObjectId parentId = null;
            String parent = request.getParameter("parentCommentId");
            if (parent != null && !parent.isBlank()) {
                if (!ObjectId.isValid(parent)) {
                    response.sendError(400, "Invalid parent comment ID.");
                    return;
                }
                parentId = new ObjectId(parent);
                if (CommentCollection.getCollection().find(Filters.and(
                        Filters.eq("_id", parentId),
                        Filters.eq("targetType", "blog"),
                        Filters.eq("blogId", blogId),
                        Filters.eq("status", "visible"),
                        Filters.eq("parentCommentId", null))).first() == null) {
                    response.sendError(404, "Parent comment unavailable.");
                    return;
                }
            }
            Document comment = new Document("targetType", "blog")
                    .append("blogId", blogId)
                    .append("authorId", user.getObjectId("_id"))
                    .append("authorName", user.getString("username"))
                    .append("body", body)
                    .append("status", "visible")
                    .append("createdAt", new Date());
            if (parentId != null) comment.append("parentCommentId", parentId);
            CommentCollection.getCollection().insertOne(comment);
            session.removeAttribute("blogCommentCsrfToken");
            response.sendRedirect(request.getContextPath() + "/blog/view?id="
                    + blogId.toHexString() + "&comment=posted#comments");
        } catch (Exception e) {
            getServletContext().log("Blog comment submission failed.", e);
            if (!response.isCommitted()) response.sendError(500, "Unable to post comment.");
        }
    }

    static boolean validToken(Object stored, String submitted) {
        return stored instanceof String expected && submitted != null
                && MessageDigest.isEqual(expected.getBytes(StandardCharsets.UTF_8),
                        submitted.getBytes(StandardCharsets.UTF_8));
    }
}
