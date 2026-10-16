package com.example.news.servlet;

import com.example.news.util.EditorialCollection;
import com.example.news.util.RoleUtil;
import com.example.news.util.RichTextUtil;
import com.example.news.util.CloudinaryImageUtil;

import org.bson.Document;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.annotation.MultipartConfig;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import jakarta.servlet.http.Part;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.util.Date;
import java.util.UUID;

@WebServlet("/writer/submit")
@MultipartConfig(fileSizeThreshold = 0, maxFileSize = 5L * 1024 * 1024,
        maxRequestSize = 6L * 1024 * 1024)
public class WriterArticleServlet extends HttpServlet {

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

            HttpSession session = request.getSession();

            if (session.getAttribute("articleCsrfToken") == null) {
                session.setAttribute(
                        "articleCsrfToken",
                        UUID.randomUUID().toString()
                );
            }

            request.getRequestDispatcher(
                    "/WEB-INF/views/writer-submit.jsp"
            ).forward(request, response);

        } catch (Exception e) {
            getServletContext().log(
                    "Unable to load article form.", e
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

        CloudinaryImageUtil.UploadedImage newCover = null;
        boolean storedInMongo = false;

        try {
            if (!checkAccess(request, response)) {
                return;
            }

String action = request.getParameter("action");

boolean saveDraft = "draft".equals(action);

if (!saveDraft && !"submit".equals(action)) {
    response.sendError(400, "Invalid article action.");
    return;
}

            HttpSession session = request.getSession(false);
if (session == null) {
    response.sendError(403, "Session expired.");
    return;
}

            String expected = (String)
                    session.getAttribute("articleCsrfToken");

            String submitted = request.getParameter("csrfToken");

            if (expected == null || submitted == null ||
                    !MessageDigest.isEqual(
                            expected.getBytes(StandardCharsets.UTF_8),
                            submitted.getBytes(StandardCharsets.UTF_8))) {

                response.sendError(403, "Invalid form token.");
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

if (rawHtml != null && !rawHtml.isBlank()) {

    // Limit incoming HTML size before processing.
    if (rawHtml.length() > 100000) {
        showError(request, response,
                "Formatted article content is too large.");
        return;
    }

    // Remove unsafe HTML, attributes and URLs.
    safeHtml = RichTextUtil.sanitize(rawHtml);

    // Derive searchable plain text from the safe HTML.
    content = RichTextUtil.toPlainText(safeHtml);
}

// Maximum lengths apply to both drafts and submissions.
if (title.length() > 200
        || category.length() > 60
        || content.length() > 20000) {

    showError(request, response,
            "One or more fields exceed the maximum length.");
    return;
}

if (saveDraft) {

    // Avoid creating completely empty drafts.
    if (title.isEmpty() && category.isEmpty()
            && content.isEmpty()) {

        showError(request, response,
                "Enter some content before saving a draft.");
        return;
    }

} else {

    // Full validation is required for editor submission.
    if (title.length() < 5
            || category.length() < 2
            || content.length() < 50) {

        showError(request, response,
                "To submit an article, enter a title "
                + "(at least 5 characters), category "
                + "(at least 2 characters), and content "
                + "(at least 50 characters).");
        return;
    }
}


            // Retrieve identity and current role from MongoDB.
            Document user = RoleUtil.getCurrentUser(request);

            if (!RoleUtil.hasRole(
                    user, "writer", "editor", "administrator")) {

                response.sendError(403, "Access denied.");
                return;
            }

            try {
                newCover = CloudinaryImageUtil.uploadEditorialCover(
                        request.getPart("coverImage"));
            } catch (IllegalArgumentException e) {
                showError(request, response, e.getMessage());
                return;
            } catch (IllegalStateException e) {
                response.sendError(413, "Cover image is too large.");
                return;
            }

            Date now = new Date();

            Document article = new Document()
                    .append("title", title)
                    .append("category", category)
                    .append("content", content)
                    .append("authorId", user.getObjectId("_id"))
                    .append("authorName", user.getString("username"))
                    .append("status", saveDraft ? "draft" : "pending")
                    .append("createdAt", now)
                    .append("updatedAt", now);
if (safeHtml != null) {
    article.append("contentHtml", safeHtml);
}
            if (newCover != null) {
                article.append("coverImageUrl", newCover.url());
                article.append("coverImagePublicId", newCover.publicId());
            }

            EditorialCollection.getCollection()
                    .insertOne(article);
            storedInMongo = true;

            session.removeAttribute("articleCsrfToken");

            response.sendRedirect(
        request.getContextPath()
        + (saveDraft
            ? "/writer/articles?saved=draft"
            : "/writer/submit?submitted=1")
);

        } catch (Exception e) {
            getServletContext().log(
                    "Article submission failed.", e
            );
            response.sendError(
                    500, "Unable to submit article."
            );
        } finally {
            if (!storedInMongo && newCover != null) {
                try {
                    CloudinaryImageUtil.deleteEditorialCover(
                            newCover.publicId());
                } catch (IOException cleanupError) {
                    getServletContext().log(
                            "Unused editorial cover cleanup failed.",
                            cleanupError);
                }
            }
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
                user, "writer", "editor", "administrator")) {

            response.sendError(
                    403, "Editorial access denied."
            );
            return false;
        }

        return true;
    }

    private void showError(
            HttpServletRequest request,
            HttpServletResponse response,
            String message
    ) throws ServletException, IOException {

        response.setStatus(400);
        request.setAttribute("error", message);
        request.setAttribute("formTitle", request.getParameter("title"));
        request.setAttribute("formCategory", request.getParameter("category"));
        request.setAttribute("formContent", request.getParameter("content"));
        String rawHtml = request.getParameter("contentHtml");
        if (rawHtml != null && rawHtml.length() <= 100000) {
            request.setAttribute("formContentHtml", RichTextUtil.sanitize(rawHtml));
        }

        request.getRequestDispatcher(
                "/WEB-INF/views/writer-submit.jsp"
        ).forward(request, response);
    }
}
