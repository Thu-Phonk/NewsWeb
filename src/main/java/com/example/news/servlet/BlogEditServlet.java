
package com.example.news.servlet;

import com.example.news.user.UserCollection;
import com.example.news.util.BlogCollection;
import com.mongodb.client.model.Filters;
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
import java.util.Date;
import java.util.Set;
import java.util.UUID;

@WebServlet("/blog/edit")
public class BlogEditServlet extends HttpServlet {

    private static final Set<String> ALLOWED_ROLES =
            Set.of("guest", "subscriber", "writer",
                   "editor", "administrator");

    @Override
    protected void doGet(
            HttpServletRequest request,
            HttpServletResponse response
    ) throws ServletException, IOException {

        response.setHeader("Cache-Control", "no-store");

        HttpSession session = request.getSession(false);
        ObjectId authorId = getAuthorId(session);

        if (authorId == null) {
            response.sendRedirect(
                    request.getContextPath() + "/login"
            );
            return;
        }

        String id = request.getParameter("id");

        if (!ObjectId.isValid(id)) {
            response.sendError(400, "Invalid blog ID.");
            return;
        }

        try {
            if (!isAuthorized(authorId)) {
                response.sendError(403, "Access denied.");
                return;
            }

            Document blog = BlogCollection.getCollection()
                    .find(Filters.and(
                            Filters.eq("_id", new ObjectId(id)),
                            Filters.eq("authorId", authorId)
                    ))
                    .first();

            if (blog == null) {
                response.sendError(404, "Blog not found.");
                return;
            }

            if (session.getAttribute("blogEditCsrfToken") == null) {
                session.setAttribute(
                        "blogEditCsrfToken",
                        UUID.randomUUID().toString()
                );
            }

            request.setAttribute("blog", blog);

            request.getRequestDispatcher(
                    "/WEB-INF/views/blog-edit.jsp"
            ).forward(request, response);

        } catch (Exception e) {
            getServletContext().log("Load blog edit failed.", e);
            response.sendError(500, "Unable to load blog.");
        }
    }

    @Override
    protected void doPost(
            HttpServletRequest request,
            HttpServletResponse response
    ) throws ServletException, IOException {

        request.setCharacterEncoding("UTF-8");
        response.setHeader("Cache-Control", "no-store");

        HttpSession session = request.getSession(false);
        ObjectId authorId = getAuthorId(session);

        if (authorId == null) {
            response.sendError(401, "Login required.");
            return;
        }

        String expected = (String)
                session.getAttribute("blogEditCsrfToken");
        String submitted = request.getParameter("csrfToken");

        if (expected == null || submitted == null ||
                !MessageDigest.isEqual(
                        expected.getBytes(StandardCharsets.UTF_8),
                        submitted.getBytes(StandardCharsets.UTF_8))) {
            response.sendError(403, "Invalid form token.");
            return;
        }

        String id = request.getParameter("id");

        if (!ObjectId.isValid(id)) {
            response.sendError(400, "Invalid blog ID.");
            return;
        }

        try {
            if (!isAuthorized(authorId)) {
                response.sendError(403, "Access denied.");
                return;
            }

            ObjectId blogId = new ObjectId(id);

            Document blog = BlogCollection.getCollection()
                    .find(Filters.and(
                            Filters.eq("_id", blogId),
                            Filters.eq("authorId", authorId)
                    ))
                    .first();

            if (blog == null) {
                response.sendError(404, "Blog not found.");
                return;
            }

            String title = request.getParameter("title");
            String category = request.getParameter("category");
            String content = request.getParameter("content");

            if (title == null || category == null ||
                    content == null) {
                showError(request, response, blog,
                        "All fields are required.");
                return;
            }

            title = title.trim();
            category = category.trim();
            content = content.trim();

            blog.put("title", title);
            blog.put("category", category);
            blog.put("content", content);

            if (title.length() < 3 || title.length() > 150 ||
                    category.length() < 2 || category.length() > 60 ||
                    content.length() < 10 ||
                    content.length() > 10000) {

                showError(request, response, blog,
                        "Check the title, category and content lengths.");
                return;
            }

            // Ownership is checked again in the update query.
            var result = BlogCollection.getCollection()
                    .updateOne(
                            Filters.and(
                                    Filters.eq("_id", blogId),
                                    Filters.eq("authorId", authorId)
                            ),
                            
Updates.combine(
        Updates.set("title", title),
        Updates.set("category", category),
        Updates.set("content", content),
        Updates.set("status", "pending"),
        Updates.set("updatedAt", new Date()),
        Updates.unset("reviewedBy"),
        Updates.unset("reviewedAt"),
        Updates.unset("rejectionReason")
)

                    );

            if (result.getMatchedCount() != 1) {
                response.sendError(404, "Blog not found.");
                return;
            }

            session.removeAttribute("blogEditCsrfToken");

            response.sendRedirect(
                    request.getContextPath()
                    + "/blog/my-posts?updated=1"
            );

        } catch (Exception e) {
            getServletContext().log("Blog editing failed.", e);
            response.sendError(500, "Unable to update blog.");
        }
    }

    private ObjectId getAuthorId(HttpSession session) {
        if (session == null) {
            return null;
        }

        Object userId = session.getAttribute("userId");

        if (!(userId instanceof String id)
                || !ObjectId.isValid(id)) {
            return null;
        }

        return new ObjectId(id);
    }

    private boolean isAuthorized(ObjectId authorId) {
        Document user = UserCollection.getUsers()
                .find(Filters.eq("_id", authorId))
                .first();

        return user != null
                && ALLOWED_ROLES.contains(user.getString("role"));
    }

    private void showError(
            HttpServletRequest request,
            HttpServletResponse response,
            Document blog,
            String message
    ) throws ServletException, IOException {

        response.setStatus(400);
        request.setAttribute("blog", blog);
        request.setAttribute("error", message);

        request.getRequestDispatcher(
                "/WEB-INF/views/blog-edit.jsp"
        ).forward(request, response);
    }
}
