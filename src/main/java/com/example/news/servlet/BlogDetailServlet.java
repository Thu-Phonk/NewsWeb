package com.example.news.servlet;

import com.example.news.util.BlogCollection;
import com.example.news.util.CommentCollection;
import com.example.news.util.RichTextUtil;
import com.example.news.util.RoleUtil;
import com.example.news.util.ReactionUtil;
import com.mongodb.client.model.Filters;
import com.mongodb.client.model.Sorts;
import org.bson.Document;
import org.bson.types.ObjectId;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@WebServlet("/blog/view")
public class BlogDetailServlet extends HttpServlet {
    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        try {
            String id = request.getParameter("id");
            if (id == null || !ObjectId.isValid(id)) {
                response.sendError(400, "Invalid blog ID.");
                return;
            }
            ObjectId blogId = new ObjectId(id);
            Document blog = BlogCollection.getCollection().find(Filters.and(
                    Filters.eq("_id", blogId), Filters.eq("status", "published"))).first();
            if (blog == null) {
                response.sendError(404, "Published blog not found.");
                return;
            }
            String storedHtml = blog.getString("contentHtml");
            if (storedHtml != null && !storedHtml.isBlank()) {
                blog.put("safeContentHtml", RichTextUtil.sanitize(storedHtml));
            }

            Document viewer = RoleUtil.getCurrentUser(request);
            boolean moderator = RoleUtil.hasRole(viewer, "editor", "administrator");
            var statusFilter = moderator
                    ? Filters.in("status", "visible", "hidden")
                    : Filters.eq("status", "visible");
            var scope = Filters.and(Filters.eq("targetType", "blog"),
                    Filters.eq("blogId", blogId), statusFilter);

            List<Document> comments = new ArrayList<>();
            List<ObjectId> parentIds = new ArrayList<>();
            Map<ObjectId, List<Document>> replyLists = new HashMap<>();

            for (Document comment : CommentCollection.getCollection()
                    .find(Filters.and(scope, Filters.eq("parentCommentId", null)))
                    .sort(Sorts.descending("createdAt", "_id")).limit(30)) {
                setOwnerPermission(comment, viewer);
                List<Document> replies = new ArrayList<>();
                comment.put("replies", replies);
                replyLists.put(comment.getObjectId("_id"), replies);
                parentIds.add(comment.getObjectId("_id"));
                comments.add(comment);
            }
            if (!parentIds.isEmpty()) {
                for (Document reply : CommentCollection.getCollection()
                        .find(Filters.and(scope, Filters.in("parentCommentId", parentIds)))
                        .sort(Sorts.ascending("createdAt", "_id")).limit(200)) {
                    setOwnerPermission(reply, viewer);
                    List<Document> replies = replyLists.get(reply.getObjectId("parentCommentId"));
                    if (replies != null) replies.add(reply);
                }
            }

            if (viewer != null) {
                HttpSession session = request.getSession(false);
                if (session != null) {
                    if (session.getAttribute("blogCommentCsrfToken") == null) {
                        session.setAttribute("blogCommentCsrfToken", UUID.randomUUID().toString());
                    }
                    if (moderator && session.getAttribute("blogModerationCsrfToken") == null) {
                        session.setAttribute("blogModerationCsrfToken", UUID.randomUUID().toString());
                    }
                }
            }
            response.setHeader("Cache-Control", "no-store");
            
request.setAttribute("blog", blog);
request.setAttribute("comments", comments);
request.setAttribute("commentUser", viewer);
request.setAttribute("isCommentModerator", moderator);

// Load likes, bookmarks, and the current user's
// reaction status from MongoDB.
ReactionUtil.prepare(request, "blog", blogId);

request.getRequestDispatcher(
        "/WEB-INF/views/blog-detail.jsp"
).forward(request, response);

        } catch (Exception e) {
            getServletContext().log("Unable to display blog discussion.", e);
            if (!response.isCommitted()) response.sendError(500, "Unable to load blog.");
        }
    }

    private void setOwnerPermission(Document comment, Document viewer) {
        boolean owner = viewer != null
                && "visible".equals(comment.getString("status"))
                && viewer.getObjectId("_id") != null
                && viewer.getObjectId("_id").equals(comment.getObjectId("authorId"));
        comment.put("canManage", owner);
    }
}
