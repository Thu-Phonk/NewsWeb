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

@WebServlet("/blog/comment/moderate")
public class BlogCommentModerationServlet extends HttpServlet {
    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws IOException {
        request.setCharacterEncoding("UTF-8");
        response.setHeader("Cache-Control", "no-store");
        try {
            Document user = RoleUtil.getCurrentUser(request);
            if (user == null) { response.sendError(401, "Login required."); return; }
            if (!RoleUtil.hasRole(user, "editor", "administrator")) {
                response.sendError(403, "Moderator role required."); return;
            }
            HttpSession session = request.getSession(false);
            if (session == null || !BlogCommentServlet.validToken(
                    session.getAttribute("blogModerationCsrfToken"),
                    request.getParameter("csrfToken"))) {
                response.sendError(403, "Invalid moderation token."); return;
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
            if (!"hide".equals(action) && !"restore".equals(action)) {
                response.sendError(400, "Invalid action."); return;
            }
            boolean hiding = "hide".equals(action);
            String reason = request.getParameter("moderationReason");
            reason = reason == null ? "" : reason.trim();
            if (hiding && (reason.length() < 5 || reason.length() > 500)) {
                response.sendError(400, "Reason must be 5-500 characters."); return;
            }
            var filter = Filters.and(
                    Filters.eq("_id", new ObjectId(commentId)),
                    Filters.eq("targetType", "blog"),
                    Filters.eq("blogId", blogId),
                    Filters.eq("status", hiding ? "visible" : "hidden"));
            var update = Updates.combine(
                    Updates.set("status", hiding ? "hidden" : "visible"),
                    Updates.set("moderatedBy", user.getObjectId("_id")),
                    Updates.set("moderatedAt", new Date()),
                    Updates.set("updatedAt", new Date()),
                    hiding ? Updates.set("moderationReason", reason)
                            : Updates.unset("moderationReason"));
            if (CommentCollection.getCollection().updateOne(filter, update)
                    .getMatchedCount() != 1) {
                response.sendError(409, "Comment not available for this action."); return;
            }
            session.removeAttribute("blogModerationCsrfToken");
            response.sendRedirect(request.getContextPath() + "/blog/view?id="
                    + blogId.toHexString() + "&moderated="
                    + (hiding ? "hidden" : "restored") + "#comments");
        } catch (Exception e) {
            getServletContext().log("Blog comment moderation failed.", e);
            if (!response.isCommitted()) response.sendError(500, "Unable to moderate comment.");
        }
    }
}
