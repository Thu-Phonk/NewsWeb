
package com.example.news.servlet;

import com.example.news.user.UserCollection;
import com.example.news.util.BlogCollection;
import com.example.news.util.RichTextUtil;

import com.mongodb.client.model.Filters;
import com.mongodb.client.model.Projections;

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

@WebServlet("/blog/create")
public class BlogCreateServlet extends HttpServlet {

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

        if (session == null
                || session.getAttribute("userId") == null) {
            response.sendRedirect(
                    request.getContextPath() + "/login"
            );
            return;
        }

        try {
            Document user = loadUser(session);

            if (user == null) {
                response.sendError(403, "Access denied.");
                return;
            }

            if (session.getAttribute("blogCsrfToken") == null) {
                session.setAttribute(
                        "blogCsrfToken",
                        UUID.randomUUID().toString()
                );
            }

            request.getRequestDispatcher(
                    "/WEB-INF/views/blog-create.jsp"
            ).forward(request, response);

        } catch (Exception e) {
            getServletContext().log(
                    "Unable to load blog form.", e
            );
            response.sendError(500);
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

        if (session == null
                || session.getAttribute("userId") == null) {
            response.sendRedirect(
                    request.getContextPath() + "/login"
            );
            return;
        }

        String expectedToken =
                (String) session.getAttribute("blogCsrfToken");
        String submittedToken =
                request.getParameter("csrfToken");

        if (expectedToken == null || submittedToken == null ||
                !MessageDigest.isEqual(
                        expectedToken.getBytes(StandardCharsets.UTF_8),
                        submittedToken.getBytes(StandardCharsets.UTF_8))) {
            response.sendError(403, "Invalid form token.");
            return;
        }

        
String action = request.getParameter("action");

boolean saveDraft = "draft".equals(action);

if (!saveDraft && !"submit".equals(action)) {
    response.sendError(400, "Invalid blog action.");
    return;
}

String title = request.getParameter("title");
String category = request.getParameter("category");
String content = request.getParameter("content");

title = title == null ? "" : title.trim();
category = category == null ? "" : category.trim();
content = content == null ? "" : content.trim();

String rawHtml = request.getParameter("contentHtml");

String safeHtml = null;

// An HTML character-length limit also prevents
// excessively large incoming formatted content.
if (rawHtml != null && !rawHtml.isBlank()) {

    if (rawHtml.length() > 50000) {
        showError(request, response,
                "Formatted content is too large.");
        return;
    }

    safeHtml = RichTextUtil.sanitize(rawHtml);

    // Derive plain content from sanitized HTML.
    // Do not trust the browser's separate content field.
    content = RichTextUtil.toPlainText(safeHtml);
}

// Apply maximum lengths to both drafts and submissions.
if (title.length() > 150 ||
        category.length() > 60 ||
        content.length() > 10000) {

    showError(request, response,
            "One or more fields are too long.");
    return;
}

if (saveDraft) {

    // Permit incomplete drafts, but not empty ones.
    if (title.isEmpty() &&
            category.isEmpty() &&
            content.isEmpty()) {

        showError(request, response,
                "Enter something before saving a draft.");
        return;
    }

} else {

    // A submitted blog must be complete.
    if (title.length() < 3 ||
            category.length() < 2 ||
            content.length() < 10) {

        showError(request, response,
                "To submit your blog, enter a title "
                + "(at least 3 characters), "
                + "category (at least 2 characters), "
                + "and content (at least 10 characters).");
        return;
    }
}


        try {
            Document user = loadUser(session);

            if (user == null) {
                response.sendError(403, "Access denied.");
                return;
            }

            Document blog = new Document()
                    .append("title", title)
                    .append("category", category)
                    .append("content", content)
                    .append("authorId", user.getObjectId("_id"))
                    .append("authorName", user.getString("username"))
                    .append("status", saveDraft ? "draft" : "pending")
		    .append("createdAt", new Date())
		    .append("updatedAt", new Date());

if (safeHtml != null) {
    blog.append("contentHtml", safeHtml);
}

            BlogCollection.getCollection().insertOne(blog);

            session.removeAttribute("blogCsrfToken");

            response.sendRedirect(
        request.getContextPath()
        + (saveDraft
            ? "/blog/my-posts?saved=draft"
            : "/blog/create?created=1")
);

        } catch (Exception e) {
            getServletContext().log(
                    "Blog creation failed.", e
            );
            response.sendError(500, "Unable to create blog.");
        }
    }

    private Document loadUser(HttpSession session) {
        Object value = session.getAttribute("userId");

        if (!(value instanceof String userId)
                || !ObjectId.isValid(userId)) {
            return null;
        }

        Document user = UserCollection.getUsers()
                .find(Filters.eq("_id", new ObjectId(userId)))
                .projection(Projections.include("username", "role"))
                .first();

        if (user == null ||
                !ALLOWED_ROLES.contains(user.getString("role"))) {
            return null;
        }

        return user;
    }

    private void showError(
            HttpServletRequest request,
            HttpServletResponse response,
            String error
    ) throws ServletException, IOException {

        response.setStatus(400);
        request.setAttribute("error", error);

        request.getRequestDispatcher(
                "/WEB-INF/views/blog-create.jsp"
        ).forward(request, response);
    }
}
