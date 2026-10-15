
package com.example.news.servlet;

import com.example.news.util.EditorialCollection;
import com.example.news.util.RoleUtil;
import com.example.news.util.RichTextUtil;

import com.mongodb.client.model.Filters;
import com.mongodb.client.model.Sorts;
import com.mongodb.client.model.Updates;

import org.bson.Document;
import org.bson.types.ObjectId;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.UUID;

@WebServlet("/editor")
public class EditorReviewServlet extends HttpServlet {

    @Override
    protected void doGet(
            HttpServletRequest request,
            HttpServletResponse response
    ) throws ServletException, IOException {

        response.setHeader("Cache-Control", "no-store");

        try {
            if (!checkAccess(request, response)) {
                return;
            }

            HttpSession session = request.getSession(false);

            if (session.getAttribute("editorCsrfToken") == null) {
                session.setAttribute(
                        "editorCsrfToken",
                        UUID.randomUUID().toString()
                );
            }

            List<Document> pendingArticles =
                    new ArrayList<>();

            
for (Document article :
        EditorialCollection.getCollection()
            .find(Filters.and(
                Filters.eq("status", "pending"),
                Filters.exists("authorId", true),
                Filters.ne(
                    "authorId",
                    RoleUtil.getCurrentUser(request)
                        .getObjectId("_id")
                )
            ))
            .sort(Sorts.ascending("createdAt"))
            .limit(50)) {

    // Retrieve formatted article content.
    String storedHtml =
            article.getString("contentHtml");

    // Only prepare HTML if the article contains it.
    if (storedHtml != null && !storedHtml.isBlank()) {

        // Sanitize the HTML before displaying it.
        String safeHtml =
                RichTextUtil.sanitize(storedHtml);

        article.put("safeContentHtml", safeHtml);
    }

    pendingArticles.add(article);
}


            request.setAttribute(
                    "pendingArticles", pendingArticles
            );

            request.getRequestDispatcher(
                    "/WEB-INF/views/editor.jsp"
            ).forward(request, response);

        } catch (Exception e) {
            getServletContext().log(
                    "Unable to load editor dashboard.", e
            );
            response.sendError(500);
        }
    }

    @Override
    protected void doPost(
            HttpServletRequest request,
            HttpServletResponse response
    ) throws IOException {

        request.setCharacterEncoding("UTF-8");
        response.setHeader("Cache-Control", "no-store");

        try {
            if (!checkAccess(request, response)) {
                return;
            }

            HttpSession session = request.getSession(false);

            String expected = (String)
                    session.getAttribute("editorCsrfToken");

            String submitted =
                    request.getParameter("csrfToken");

            if (expected == null || submitted == null ||
                    !MessageDigest.isEqual(
                            expected.getBytes(StandardCharsets.UTF_8),
                            submitted.getBytes(StandardCharsets.UTF_8))) {

                response.sendError(403, "Invalid form token.");
                return;
            }

            String id = request.getParameter("id");
            String action = request.getParameter("action");

            if (id == null || !ObjectId.isValid(id)) {
                response.sendError(400, "Invalid article ID.");
                return;
            }

            if (!"approve".equals(action)
                    && !"reject".equals(action)) {

                response.sendError(400, "Invalid review action.");
                return;
            }

            String rejectionReason =
                    request.getParameter("rejectionReason");

            if ("reject".equals(action)) {

                if (rejectionReason == null) {
                    rejectionReason = "";
                }

                rejectionReason = rejectionReason.trim();

                if (rejectionReason.length() < 5
                        || rejectionReason.length() > 500) {

                    response.sendError(
                            400,
                            "Rejection reason must be 5-500 characters."
                    );
                    return;
                }
            }

            // Read the reviewer's identity from MongoDB,
            // not from a browser-supplied field.
            Document reviewer =
                    RoleUtil.getCurrentUser(request);

            if (!RoleUtil.hasRole(
                    reviewer, "editor", "administrator")) {

                response.sendError(403, "Access denied.");
                return;
            }

            boolean approved = "approve".equals(action);

            Date now = new Date();

            var update = Updates.combine(
                    Updates.set(
                            "status",
                            approved ? "published" : "rejected"
                    ),
                    Updates.set("reviewedBy",
                            reviewer.getObjectId("_id")),
                    Updates.set("reviewedAt", now),
                    Updates.set("updatedAt", now),
                    approved
                        ? Updates.unset("rejectionReason")
                        : Updates.set(
                                "rejectionReason", rejectionReason
                        )
            );

            // Only pending articles can be reviewed.
            // This condition makes the transition atomic.
            
var result = EditorialCollection.getCollection()
        .updateOne(
                Filters.and(
                        Filters.eq("_id", new ObjectId(id)),

                        Filters.eq("status", "pending"),

                        // The reviewer cannot be the author.
                        Filters.exists("authorId", true),

                        Filters.ne(
                                "authorId",
                                reviewer.getObjectId("_id")
                        )
                ),
                update
        );


            if (result.getMatchedCount() != 1) {

                response.sendError(
        409,
        "Article cannot be reviewed. It may already be reviewed, "
        + "or you may be its author."
);
                return;
            }

            response.sendRedirect(
                    request.getContextPath()
                    + "/editor?reviewed="
                    + (approved ? "approved" : "rejected")
            );

        } catch (Exception e) {
            getServletContext().log(
                    "Editorial review failed.", e
            );
            response.sendError(
                    500, "Unable to review article."
            );
        }
    }

    private boolean checkAccess(
            HttpServletRequest request,
            HttpServletResponse response
    ) throws IOException {

        Document user = RoleUtil.getCurrentUser(request);

        if (user == null) {
            response.sendRedirect(
                    request.getContextPath() + "/login"
            );
            return false;
        }

        if (!RoleUtil.hasRole(
                user, "editor", "administrator")) {

            response.sendError(
                    403, "Editor permission required."
            );
            return false;
        }

        return true;
    }
}
