
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

@WebServlet("/editorial/comment/moderate")
public class EditorialCommentModerationServlet
        extends HttpServlet {

    private static final String TOKEN_KEY =
            "commentModerationCsrfToken";

    @Override
    protected void doPost(
            HttpServletRequest request,
            HttpServletResponse response
    ) throws IOException {

        request.setCharacterEncoding("UTF-8");
        response.setHeader("Cache-Control", "no-store");

        try {

            // 1. Authentication and role verification.
            Document moderator =
                    RoleUtil.getCurrentUser(request);

            if (moderator == null) {
                response.sendError(401, "Login required.");
                return;
            }

            if (!RoleUtil.hasRole(
                    moderator, "editor", "administrator")) {

                response.sendError(
                        403, "Moderator permission required."
                );
                return;
            }

            // 2. Verify CSRF token.
            HttpSession session =
                    request.getSession(false);

            if (session == null) {
                response.sendError(403, "Session expired.");
                return;
            }

            Object token =
                    session.getAttribute(TOKEN_KEY);

            String submitted =
                    request.getParameter("csrfToken");

            if (!(token instanceof String expected)
                    || submitted == null
                    || !MessageDigest.isEqual(
                            expected.getBytes(
                                    StandardCharsets.UTF_8),
                            submitted.getBytes(
                                    StandardCharsets.UTF_8))) {

                response.sendError(
                        403, "Invalid moderation token."
                );
                return;
            }

            // 3. Validate article ID and comment ID.
            String articleIdText =
                    request.getParameter("articleId");

            String commentIdText =
                    request.getParameter("commentId");

            if (articleIdText == null
                    || !ObjectId.isValid(articleIdText)
                    || commentIdText == null
                    || !ObjectId.isValid(commentIdText)) {

                response.sendError(400, "Invalid ID.");
                return;
            }

            ObjectId articleId =
                    new ObjectId(articleIdText);

            ObjectId commentId =
                    new ObjectId(commentIdText);

            // 4. Validate moderation action.
            String action =
                    request.getParameter("action");

            if (!"hide".equals(action)
                    && !"restore".equals(action)) {

                response.sendError(
                        400, "Invalid moderation action."
                );
                return;
            }

            boolean hiding = "hide".equals(action);

            String reason =
                    request.getParameter("moderationReason");

            reason = reason == null ? "" : reason.trim();

            if (hiding
                    && (reason.length() < 5
                    || reason.length() > 500)) {

                response.sendError(
                        400,
                        "Moderation reason must be 5-500 characters."
                );
                return;
            }

            // 5. Only moderate comments on published articles.
            Document article =
                    EditorialCollection.getCollection()
                            .find(Filters.and(
                                    Filters.eq("_id", articleId),
                                    Filters.eq("status", "published")
                            ))
                            .first();

            if (article == null) {
                response.sendError(
                        404, "Published article not found."
                );
                return;
            }

            // 6. Only transition from the expected status.
            String oldStatus =
                    hiding ? "visible" : "hidden";

            String newStatus =
                    hiding ? "hidden" : "visible";

            var commentFilter = Filters.and(
                    Filters.eq("_id", commentId),
                    Filters.eq("articleId", articleId),
                    Filters.eq("status", oldStatus)
            );

            Date now = new Date();

            var update = Updates.combine(
                    Updates.set("status", newStatus),
                    Updates.set(
                            "moderatedBy",
                            moderator.getObjectId("_id")
                    ),
                    Updates.set("moderatedAt", now),
                    Updates.set("updatedAt", now),
                    Updates.set(
                            "moderationAction",
                            action
                    ),
                    hiding
                            ? Updates.set(
                                    "moderationReason", reason)
                            : Updates.unset("moderationReason")
            );

            // 7. Atomic update prevents changing a deleted
            // comment or repeating an outdated action.
            long matched =
                    CommentCollection.getCollection()
                            .updateOne(commentFilter, update)
                            .getMatchedCount();

            if (matched != 1) {
                response.sendError(
                        409,
                        "Comment not found or status changed."
                );
                return;
            }

            // 8. Rotate the moderation token.
            session.removeAttribute(TOKEN_KEY);

            response.sendRedirect(
                    request.getContextPath()
                    + "/editorial?id="
                    + articleId.toHexString()
                    + "&moderated="
                    + (hiding ? "hidden" : "restored")
                    + "#comments"
            );

        } catch (Exception e) {

            getServletContext().log(
                    "Editorial comment moderation failed.", e
            );

            if (!response.isCommitted()) {
                response.sendError(
                        500, "Unable to moderate comment."
                );
            }
        }
    }
}
