package com.example.news.servlet;

import com.example.news.util.BlogCollection;
import com.example.news.util.CommentCollection;
import com.example.news.util.RoleUtil;
import com.mongodb.client.model.Filters;
import com.mongodb.client.model.Updates;
import org.bson.Document;
import org.bson.types.ObjectId;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import java.io.IOException;
import java.util.Date;

@WebServlet("/blog/comment/manage")
public class BlogCommentManageServlet extends HttpServlet {
    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws IOException {
        request.setCharacterEncoding("UTF-8");
        response.setHeader("Cache-Control", "no-store");
        try {
            Document user = RoleUtil.getCurrentUser(request);
            if (user == null) { response.sendError(401, "Login required."); return; }
            HttpSession session = request.getSession(false);
            if (session == null || !BlogCommentServlet.validToken(
                    session.getAttribute("blogCommentCsrfToken"),
                    request.getParameter("csrfToken"))) {
                response.sendError(403, "Invalid comment form token."); return;
            }
            String id = request.getParameter("blogId");
            String commentId = request.getParameter("commentId");
            if (id == null || commentId == null
                    || !ObjectId.isValid(id) || !ObjectId.isValid(commentId)) {
                response.sendError(400, "Invalid ID."); return;
            }
            ObjectId blogId = new ObjectId(id);
            if (BlogCollection.getCollection().find(Filters.and(
                    Filters.eq("_id", blogId), Filters.eq("status", "published"))).first() == null) {
                response.sendError(404, "Published blog not found."); return;
            }
            String action = request.getParameter("action");
            if (!"edit".equals(action) && !"delete".equals(action)) {
                response.sendError(400, "Invalid action."); return;
            }
            var ownedComment = Filters.and(
                    Filters.eq("_id", new ObjectId(commentId)),
                    Filters.eq("targetType", "blog"),
                    Filters.eq("blogId", blogId),
                    Filters.eq("authorId", user.getObjectId("_id")),
                    Filters.eq("status", "visible"));
            long matched;
            if ("edit".equals(action)) {
                String body = request.getParameter("body");
                body = body == null ? "" : body.trim();
                if (body.length() < 2 || body.length() > 2000) {
                    response.sendError(400, "Comment must be 2-2000 characters."); return;
                }
                matched = CommentCollection.getCollection().updateOne(ownedComment,
                        Updates.combine(Updates.set("body", body),
                                Updates.set("updatedAt", new Date()))).getMatchedCount();
            } else {
                matched = CommentCollection.getCollection().updateOne(ownedComment,
                        Updates.combine(Updates.set("status", "deleted"),
                                Updates.unset("body"),
                                Updates.set("updatedAt", new Date()))).getMatchedCount();
            }
            if (matched != 1) {
                response.sendError(404, "Comment not found or not yours."); return;
            }
            session.removeAttribute("blogCommentCsrfToken");
            response.sendRedirect(request.getContextPath() + "/blog/view?id="
                    + blogId.toHexString() + "&comment="
                    + ("edit".equals(action) ? "edited" : "deleted") + "#comments");
        } catch (Exception e) {
            getServletContext().log("Blog comment management failed.", e);
            if (!response.isCommitted()) response.sendError(500, "Unable to manage comment.");
        }
    }
}
