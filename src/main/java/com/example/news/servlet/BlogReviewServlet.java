
package com.example.news.servlet;

import com.example.news.util.BlogCollection;
import com.example.news.util.RoleUtil;

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

@WebServlet("/editor/blogs")
public class BlogReviewServlet extends HttpServlet {

    @Override
    protected void doGet(
            HttpServletRequest request,
            HttpServletResponse response
    ) throws ServletException, IOException {

        response.setHeader("Cache-Control", "no-store");

        try {
            Document reviewer = getReviewer(request, response);

            if (reviewer == null) {
                return;
            }

            HttpSession session = request.getSession(false);

            if (session.getAttribute("blogReviewCsrfToken") == null) {
                session.setAttribute(
                        "blogReviewCsrfToken",
                        UUID.randomUUID().toString()
                );
            }

            List<Document> pendingBlogs = new ArrayList<>();

            // Do not offer approval of the editor's own blogs.
            for (Document blog :
                    BlogCollection.getCollection()
                        .find(Filters.and(
                                Filters.eq("status", "pending"),
                                Filters.exists("authorId", true),
                                Filters.ne(
                                        "authorId",
                                        reviewer.getObjectId("_id")
                                )
                        ))
                        .sort(Sorts.ascending("createdAt"))
                        .limit(50)) {

                pendingBlogs.add(blog);
            }

            request.setAttribute("pendingBlogs", pendingBlogs);

            request.getRequestDispatcher(
                    "/WEB-INF/views/editor-blogs.jsp"
            ).forward(request, response);

        } catch (Exception e) {
            getServletContext().log(
                    "Unable to load pending blogs.", e
            );
            response.sendError(500, "Unable to load blogs.");
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
            Document reviewer = getReviewer(request, response);

            if (reviewer == null) {
                return;
            }

            HttpSession session = request.getSession(false);

            Object stored =
                    session.getAttribute("blogReviewCsrfToken");

            String submitted = request.getParameter("csrfToken");

            if (!(stored instanceof String expected)
                    || submitted == null
                    || !MessageDigest.isEqual(
                            expected.getBytes(StandardCharsets.UTF_8),
                            submitted.getBytes(StandardCharsets.UTF_8))) {

                response.sendError(403, "Invalid form token.");
                return;
            }

            String blogId = request.getParameter("id");
            String action = request.getParameter("action");

            if (blogId == null || !ObjectId.isValid(blogId)) {
                response.sendError(400, "Invalid blog ID.");
                return;
            }

            if (!"approve".equals(action)
                    && !"reject".equals(action)) {
                response.sendError(400, "Invalid action.");
                return;
            }

            boolean approved = "approve".equals(action);

            String reason = request.getParameter("rejectionReason");

            if (!approved) {
                reason = reason == null ? "" : reason.trim();

                if (reason.length() < 5 || reason.length() > 500) {
                    response.sendError(
                            400,
                            "Rejection reason must be 5-500 characters."
                    );
                    return;
                }
            }

            Date now = new Date();

            var update = Updates.combine(
                    Updates.set(
                            "status",
                            approved ? "published" : "rejected"
                    ),
                    Updates.set(
                            "reviewedBy",
                            reviewer.getObjectId("_id")
                    ),
                    Updates.set("reviewedAt", now),
                    Updates.set("updatedAt", now),
                    approved
                        ? Updates.unset("rejectionReason")
                        : Updates.set("rejectionReason", reason)
            );

            // Atomic permission and status checks.
            var result = BlogCollection.getCollection()
                    .updateOne(
                            Filters.and(
                                    Filters.eq(
                                            "_id",
                                            new ObjectId(blogId)
                                    ),
                                    Filters.eq("status", "pending"),
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
                        "Blog is unavailable, already reviewed, "
                        + "or belongs to you."
                );
                return;
            }

            response.sendRedirect(
                    request.getContextPath()
                    + "/editor/blogs?reviewed="
                    + (approved ? "approved" : "rejected")
            );

        } catch (Exception e) {
            getServletContext().log(
                    "Blog review failed.", e
            );
            response.sendError(500, "Unable to review blog.");
        }
    }

    private Document getReviewer(
            HttpServletRequest request,
            HttpServletResponse response
    ) throws IOException {

        Document user = RoleUtil.getCurrentUser(request);

        if (user == null) {
            response.sendRedirect(
                    request.getContextPath() + "/login"
            );
            return null;
        }

        if (!RoleUtil.hasRole(
                user, "editor", "administrator")) {
            response.sendError(
                    403, "Editor permission required."
            );
            return null;
        }

        return user;
    }
}
