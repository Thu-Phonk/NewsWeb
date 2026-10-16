
package com.example.news.servlet;

import com.example.news.util.CommentCollection;
import com.example.news.util.EditorialCollection;
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

@WebServlet("/editorial/comment")
public class EditorialCommentServlet extends HttpServlet {

    private static final String TOKEN_KEY =
            "editorialCommentCsrfToken";

    private static final int MAX_LENGTH = 2000;

    @Override
    protected void doPost(
            HttpServletRequest request,
            HttpServletResponse response
    ) throws IOException {

        request.setCharacterEncoding("UTF-8");
        response.setHeader("Cache-Control", "no-store");

        try {

            // 1. Require a valid, currently existing user.
            Document user = RoleUtil.getCurrentUser(request);

            if (user == null) {
                response.sendRedirect(
                        request.getContextPath() + "/login"
                );
                return;
            }

            // 2. Validate session and CSRF token.
            HttpSession session = request.getSession(false);

            if (session == null) {
                response.sendError(403, "Session expired.");
                return;
            }

            Object storedToken =
                    session.getAttribute(TOKEN_KEY);

            String submittedToken =
                    request.getParameter("csrfToken");

            if (!(storedToken instanceof String expected)
                    || submittedToken == null
                    || !MessageDigest.isEqual(
                            expected.getBytes(
                                    StandardCharsets.UTF_8),
                            submittedToken.getBytes(
                                    StandardCharsets.UTF_8))) {

                response.sendError(
                        403, "Invalid comment form token."
                );
                return;
            }

            // 3. Validate the article ID.
            String id = request.getParameter("articleId");

            if (id == null || !ObjectId.isValid(id)) {
                response.sendError(400, "Invalid article ID.");
                return;
            }

            ObjectId articleId = new ObjectId(id);

            // 4. Only allow comments on published articles.
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

            // 5. Validate plain-text comment content.
            String body = request.getParameter("body");

            body = body == null ? "" : body.trim();

            if (body.length() < 2
                    || body.length() > MAX_LENGTH) {

                response.sendError(
                        400,
                        "Comments must contain 2 to 2000 characters."
                );
                return;
            }

            // 6. Use the account details from MongoDB,
            // never a username supplied by the browser.

ObjectId parentCommentId = null;

String parentIdText =
        request.getParameter("parentCommentId");

if (parentIdText != null && !parentIdText.isBlank()) {

    if (!ObjectId.isValid(parentIdText)) {
        response.sendError(400, "Invalid parent comment ID.");
        return;
    }

    parentCommentId = new ObjectId(parentIdText);

    // Parent must be a visible, top-level comment
    // belonging to the same published article.
    Document parent = CommentCollection.getCollection()
            .find(Filters.and(
                    Filters.eq("_id", parentCommentId),
                    Filters.eq("articleId", articleId),
                    Filters.eq("status", "visible"),
                    Filters.eq("parentCommentId", null)
            ))
            .first();

    if (parent == null) {
        response.sendError(
                404, "Comment is unavailable for replies."
        );
        return;
    }
}

            Document comment = new Document()
                    .append("articleId", articleId)
                    .append("authorId", user.getObjectId("_id"))
                    .append("authorName",
                            user.getString("username"))
                    .append("body", body)
                    .append("status", "visible")
                    .append("createdAt", new Date());

if (parentCommentId != null) {
    comment.append("parentCommentId", parentCommentId);
}

            CommentCollection.getCollection()
                    .insertOne(comment);

            // 7. Make this CSRF token single-use.
            session.removeAttribute(TOKEN_KEY);

            // 8. Redirect to the article's comment section.
            response.sendRedirect(
                    request.getContextPath()
                    + "/editorial?id=" + articleId.toHexString()
                    + "&comment=posted#comments"
            );

        } catch (Exception e) {

            getServletContext().log(
                    "Unable to post editorial comment.", e
            );

            if (!response.isCommitted()) {
                response.sendError(
                        500, "Unable to post comment."
                );
            }
        }
    }
}
