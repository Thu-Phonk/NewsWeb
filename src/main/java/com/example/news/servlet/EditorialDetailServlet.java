
package com.example.news.servlet;

import com.example.news.util.CommentCollection;
import com.example.news.util.EditorialCollection;
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
import java.util.List;
import java.util.UUID;
import java.util.HashMap;
import java.util.Map;

@WebServlet("/editorial")
public class EditorialDetailServlet extends HttpServlet {

    @Override
    protected void doGet(
            HttpServletRequest request,
            HttpServletResponse response
    ) throws ServletException, IOException {

        try {

            String id = request.getParameter("id");

            if (id == null || !ObjectId.isValid(id)) {
                response.sendError(
                        400, "Invalid article ID."
                );
                return;
            }

            ObjectId articleId = new ObjectId(id);

            // Public access requires published status.
            Document article =
                    EditorialCollection.getCollection()
                            .find(Filters.and(
                                    Filters.eq("_id", articleId),
                                    Filters.eq("status", "published")
                            ))
                            .first();

            if (article == null) {
                response.sendError(
                        404, "Article not found."
                );
                return;
            }

            // Sanitize editorial HTML before rendering.
            String storedHtml =
                    article.getString("contentHtml");

            if (storedHtml != null
                    && !storedHtml.isBlank()) {

                article.put(
                        "safeContentHtml",
                        RichTextUtil.sanitize(storedHtml)
                );
            }

            // Identify the user and moderator role.
            Document commentUser =
                    RoleUtil.getCurrentUser(request);

            boolean isModerator =
                    RoleUtil.hasRole(
                            commentUser,
                            "editor",
                            "administrator"
                    );

            
// Moderators see visible and hidden comments.
// Ordinary readers only see visible comments.
var allowedStatuses = isModerator
        ? Filters.in("status", "visible", "hidden")
        : Filters.eq("status", "visible");

// Load top-level comments, including legacy comments
// that do not have parentCommentId.
List<Document> comments = new ArrayList<>();

List<ObjectId> parentIds = new ArrayList<>();

Map<ObjectId, List<Document>> replyBuckets =
        new HashMap<>();

for (Document comment :
        CommentCollection.getCollection()
                .find(Filters.and(
                        Filters.eq("articleId", articleId),
                        allowedStatuses,
                        Filters.eq("parentCommentId", null)
                ))
                .sort(Sorts.descending("createdAt", "_id"))
                .limit(50)) {

    ObjectId commentId = comment.getObjectId("_id");

    boolean canManage = commentUser != null
            && "visible".equals(comment.getString("status"))
            && commentUser.getObjectId("_id").equals(
                    comment.getObjectId("authorId")
            );

    comment.put("canManage", canManage);

    List<Document> replies = new ArrayList<>();

    comment.put("replies", replies);

    replyBuckets.put(commentId, replies);
    parentIds.add(commentId);
    comments.add(comment);
}

// Load replies for the selected parent comments.
if (!parentIds.isEmpty()) {

    for (Document reply :
            CommentCollection.getCollection()
                    .find(Filters.and(
                            Filters.eq("articleId", articleId),
                            allowedStatuses,
                            Filters.in("parentCommentId", parentIds)
                    ))
                    .sort(Sorts.ascending("createdAt", "_id"))
                    .limit(200)) {

        boolean canManage = commentUser != null
                && "visible".equals(reply.getString("status"))
                && commentUser.getObjectId("_id").equals(
                        reply.getObjectId("authorId")
                );

        reply.put("canManage", canManage);

        ObjectId parentId =
                reply.getObjectId("parentCommentId");

        List<Document> bucket = replyBuckets.get(parentId);

        if (bucket != null) {
            bucket.add(reply);
        }
    }
}

            // Prepare CSRF tokens for authenticated users.
            if (commentUser != null) {

                HttpSession session =
                        request.getSession(false);

                if (session != null) {

                    if (session.getAttribute(
                            "editorialCommentCsrfToken") == null) {

                        session.setAttribute(
                                "editorialCommentCsrfToken",
                                UUID.randomUUID().toString()
                        );
                    }

                    if (isModerator
                            && session.getAttribute(
                                    "commentModerationCsrfToken") == null) {

                        session.setAttribute(
                                "commentModerationCsrfToken",
                                UUID.randomUUID().toString()
                        );
                    }
                }
            }

            request.setAttribute("article", article);
            request.setAttribute("comments", comments);
            request.setAttribute("commentUser", commentUser);
            request.setAttribute("isCommentModerator", isModerator);
// Load the like count and bookmark status.
ReactionUtil.prepare(request, "editorial", articleId);
            request.getRequestDispatcher(
                    "/WEB-INF/views/editorial-detail.jsp"
            ).forward(request, response);

        } catch (Exception e) {

            getServletContext().log(
                    "Unable to load editorial article.", e
            );

            if (!response.isCommitted()) {
                response.sendError(
                        500, "Unable to load article."
                );
            }
        }
    }
}
