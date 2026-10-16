
package com.example.news.servlet;

import com.example.news.util.CommentCollection;
import com.example.news.util.EditorialCollection;
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
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.util.Date;

@WebServlet("/editorial/comment/manage")
public class EditorialCommentManageServlet extends HttpServlet {

    private static final String TOKEN_KEY =
            "editorialCommentCsrfToken";

    @Override
    protected void doPost(
            HttpServletRequest request,
            HttpServletResponse response
    ) throws IOException {

        request.setCharacterEncoding("UTF-8");
        response.setHeader("Cache-Control", "no-store");

        try {
            // Verify login.
            Document user = RoleUtil.getCurrentUser(request);

            if (user == null) {
                response.sendError(401, "Login required.");
                return;
            }

            // Verify CSRF token.
            HttpSession session = request.getSession(false);

            if (session == null) {
                response.sendError(403, "Session expired.");
                return;
            }

            Object token = session.getAttribute(TOKEN_KEY);
            String submitted = request.getParameter("csrfToken");

            if (!(token instanceof String expected)
                    || submitted == null
                    || !MessageDigest.isEqual(
                            expected.getBytes(StandardCharsets.UTF_8),
                            submitted.getBytes(StandardCharsets.UTF_8))) {

                response.sendError(403, "Invalid form token.");
                return;
            }

            // Validate IDs and action.
            String commentId = request.getParameter("commentId");
            String articleId = request.getParameter("articleId");
            String action = request.getParameter("action");

            if (!ObjectId.isValid(commentId)
                    || !ObjectId.isValid(articleId)) {

                response.sendError(400, "Invalid ID.");
                return;
            }

            if (!"edit".equals(action)
                    && !"delete".equals(action)) {

                response.sendError(400, "Invalid action.");
                return;
            }

            ObjectId commentObjectId = new ObjectId(commentId);
            ObjectId articleObjectId = new ObjectId(articleId);

            // Comments are editable only while the article
            // remains published.
            Document article = EditorialCollection.getCollection()
                    .find(Filters.and(
                            Filters.eq("_id", articleObjectId),
                            Filters.eq("status", "published")
                    ))
                    .first();

            if (article == null) {
                response.sendError(404, "Article not found.");
                return;
            }

            // Require both ownership and correct article.
            var ownershipFilter = Filters.and(
                    Filters.eq("_id", commentObjectId),
                    Filters.eq("articleId", articleObjectId),
                    Filters.eq("authorId", user.getObjectId("_id")),
                    Filters.eq("status", "visible")
            );

            long matched;

            if ("edit".equals(action)) {

                String body = request.getParameter("body");
                body = body == null ? "" : body.trim();

                if (body.length() < 2 || body.length() > 2000) {
                    response.sendError(
                            400, "Comment must be 2-2000 characters."
                    );
                    return;
                }

                matched = CommentCollection.getCollection()
                        .updateOne(
                                ownershipFilter,
                                Updates.combine(
                                        Updates.set("body", body),
                                        Updates.set("updatedAt", new Date())
                                )
                        )
                        .getMatchedCount();

            } else {

                // Soft delete: preserve a record for auditing,
                // but remove the original message text.
                matched = CommentCollection.getCollection()
                        .updateOne(
                                ownershipFilter,
                                Updates.combine(
                                        Updates.set("status", "deleted"),
                                        Updates.unset("body"),
                                        Updates.set("updatedAt", new Date())
                                )
                        )
                        .getMatchedCount();
            }

            if (matched != 1) {
                response.sendError(
                        404,
                        "Comment not found or permission denied."
                );
                return;
            }

            session.removeAttribute(TOKEN_KEY);

            response.sendRedirect(
                    request.getContextPath()
                    + "/editorial?id=" + articleObjectId.toHexString()
                    + "&comment=" + ("edit".equals(action)
                            ? "edited" : "deleted")
                    + "#comments"
            );

        } catch (Exception e) {
            getServletContext().log(
                    "Comment management failed.", e
            );

            if (!response.isCommitted()) {
                response.sendError(500, "Unable to update comment.");
            }
        }
    }
}
