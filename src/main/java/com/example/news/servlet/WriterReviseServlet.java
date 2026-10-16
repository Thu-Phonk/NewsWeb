package com.example.news.servlet;

import com.example.news.util.EditorialCollection;
import com.example.news.util.RichTextUtil;
import com.example.news.util.CloudinaryImageUtil;
import com.example.news.util.RoleUtil;

import com.mongodb.client.model.Filters;
import com.mongodb.client.model.Updates;

import org.bson.Document;
import org.bson.types.ObjectId;
import org.bson.conversions.Bson;

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
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@WebServlet("/writer/revise")
@MultipartConfig(fileSizeThreshold = 0, maxFileSize = 5L * 1024 * 1024,
        maxRequestSize = 6L * 1024 * 1024)
public class WriterReviseServlet extends HttpServlet {

    private static final String VIEW =
            "/WEB-INF/views/writer-revise.jsp";

    private static final String TOKEN_KEY =
            "revisionCsrfToken";

    @Override
    protected void doGet(
            HttpServletRequest request,
            HttpServletResponse response
    ) throws ServletException, IOException {

        response.setHeader("Cache-Control", "no-store");

        try {
            Document user = requireWriter(request, response);
            if (user == null) {
                return;
            }

            ObjectId articleId =
                    parseId(request.getParameter("id"));

            if (articleId == null) {
                response.sendError(400, "Invalid article ID.");
                return;
            }

            Document article = findRejectedArticle(
                    articleId, user.getObjectId("_id"));

            if (article == null) {
                response.sendError(
                        404, "Rejected article not found."
                );
                return;
            }

            HttpSession session = request.getSession();

            if (session.getAttribute(TOKEN_KEY) == null) {
                session.setAttribute(
                        TOKEN_KEY,
                        UUID.randomUUID().toString()
                );
            }

            request.setAttribute("article", article);

            request.setAttribute(
                    "initialContentHtml",
                    RichTextUtil.sanitize(
                            article.getString("contentHtml"))
            );

            request.getRequestDispatcher(VIEW)
                    .forward(request, response);

        } catch (Exception e) {
            getServletContext().log(
                    "Unable to load rejected article.", e
            );
            response.sendError(
                    500, "Unable to load article."
            );
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
        boolean savedInMongo = false;

        try {
            Document user = requireWriter(request, response);
            if (user == null) {
                return;
            }

            HttpSession session = request.getSession(false);

            if (session == null) {
                response.sendError(403, "Session expired.");
                return;
            }

            String expected =
                    (String) session.getAttribute(TOKEN_KEY);

            String submitted =
                    request.getParameter("csrfToken");

            if (expected == null || submitted == null ||
                    !MessageDigest.isEqual(
                            expected.getBytes(
                                    StandardCharsets.UTF_8),
                            submitted.getBytes(
                                    StandardCharsets.UTF_8))) {

                response.sendError(403, "Invalid form token.");
                return;
            }

            ObjectId articleId =
                    parseId(request.getParameter("id"));

            if (articleId == null) {
                response.sendError(400, "Invalid article ID.");
                return;
            }

            ObjectId authorId = user.getObjectId("_id");

            // Only retrieve the author's rejected article.
            Document article =
                    findRejectedArticle(articleId, authorId);

            if (article == null) {
                response.sendError(
                        404,
                        "Article not found or no longer revisable."
                );
                return;
            }

            String title =
                    clean(request.getParameter("title"));

            String category =
                    clean(request.getParameter("category"));

            String content =
                    clean(request.getParameter("content"));

            String rawHtml =
                    request.getParameter("contentHtml");

            String safeHtml = null;

            if (rawHtml != null && !rawHtml.isBlank()) {

                if (rawHtml.length() > 100000) {
                    response.sendError(
                            400, "Formatted content is too large."
                    );
                    return;
                }

                safeHtml = RichTextUtil.sanitize(rawHtml);

                // Derive plain text from sanitized HTML.
                content = RichTextUtil.toPlainText(safeHtml);
            }

            // Prepare a form copy in case validation fails.
            Document formArticle = new Document(article);

            formArticle.put("title", title);
            formArticle.put("category", category);
            formArticle.put("content", content);

            if (safeHtml != null) {
                formArticle.put("contentHtml", safeHtml);
            } else {
                formArticle.remove("contentHtml");
            }

            if (title.length() > 200 ||
                    category.length() > 60 ||
                    content.length() > 20000) {

                showError(
                        request, response, formArticle,
                        "One or more fields are too long."
                );
                return;
            }

            if (title.length() < 5 ||
                    category.length() < 2 ||
                    content.length() < 50) {

                showError(
                        request, response, formArticle,
                        "Title requires 5 characters, " +
                        "category requires 2, and " +
                        "content requires 50."
                );
                return;
            }

            // Save both plain text and formatted HTML.
            Bson htmlUpdate = safeHtml == null
                    ? Updates.unset("contentHtml")
                    : Updates.set("contentHtml", safeHtml);

            // No checkbox and no new file: keep the existing image.
            boolean removeCover =
                    "true".equals(request.getParameter("removeCover"));
            Part coverPart;
            try {
                coverPart = request.getPart("coverImage");
            } catch (IllegalStateException e) {
                response.sendError(413, "Cover image is too large.");
                return;
            }
            boolean replacing = coverPart != null && coverPart.getSize() > 0;
            if (removeCover && replacing) {
                response.sendError(400,
                        "Choose either replacement or removal, not both.");
                return;
            }
            if (replacing) {
                try {
                    newCover = CloudinaryImageUtil.uploadEditorialCover(coverPart);
                } catch (IllegalArgumentException e) {
                    response.sendError(400, e.getMessage());
                    return;
                }
            }

            String previousCoverId = article.getString("coverImagePublicId");
            List<Bson> updates = new ArrayList<>();
            updates.add(Updates.set("title", title));
            updates.add(Updates.set("category", category));
            updates.add(Updates.set("content", content));
            updates.add(htmlUpdate);
            updates.add(Updates.set("status", "pending"));
            updates.add(Updates.set("updatedAt", new Date()));
            updates.add(Updates.unset("reviewedBy"));
            updates.add(Updates.unset("reviewedAt"));
            updates.add(Updates.unset("rejectionReason"));
            if (removeCover) {
                updates.add(Updates.unset("coverImageUrl"));
                updates.add(Updates.unset("coverImagePublicId"));
            } else if (newCover != null) {
                updates.add(Updates.set("coverImageUrl", newCover.url()));
                updates.add(Updates.set("coverImagePublicId", newCover.publicId()));
            }

            // Atomic check prevents revising another author's
            // article or resubmitting a non-rejected article.
            long matched = EditorialCollection.getCollection()
                    .updateOne(
                            Filters.and(
                                    Filters.eq("_id", articleId),
                                    Filters.eq("authorId", authorId),
                                    Filters.eq("status", "rejected"),
                                    Filters.eq("updatedAt", article.getDate("updatedAt"))
                            ),
                            Updates.combine(updates)
                    )
                    .getMatchedCount();

            if (matched != 1) {
                response.sendError(
                        409, "Article is no longer revisable."
                );
                return;
            }

            savedInMongo = true;
            if ((removeCover || newCover != null) && previousCoverId != null) {
                try {
                    CloudinaryImageUtil.deleteEditorialCover(previousCoverId);
                } catch (IOException cleanupError) {
                    getServletContext().log("Old editorial cover cleanup failed.", cleanupError);
                }
            }

            session.removeAttribute(TOKEN_KEY);

            response.sendRedirect(
                    request.getContextPath()
                    + "/writer/articles?submitted=1"
            );

        } catch (Exception e) {
            getServletContext().log(
                    "Unable to revise article.", e
            );
            response.sendError(
                    500, "Unable to revise article."
            );
        } finally {
            if (!savedInMongo && newCover != null) {
                try {
                    CloudinaryImageUtil.deleteEditorialCover(newCover.publicId());
                } catch (IOException cleanupError) {
                    getServletContext().log("Unused editorial cover cleanup failed.", cleanupError);
                }
            }
        }
    }

    private Document requireWriter(
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
                user, "writer", "editor", "administrator")) {

            response.sendError(
                    403, "Editorial access denied."
            );
            return null;
        }

        return user;
    }

    private Document findRejectedArticle(
            ObjectId articleId,
            ObjectId authorId
    ) {

        return EditorialCollection.getCollection()
                .find(Filters.and(
                        Filters.eq("_id", articleId),
                        Filters.eq("authorId", authorId),
                        Filters.eq("status", "rejected")
                ))
                .first();
    }

    private ObjectId parseId(String value) {
        if (value == null || !ObjectId.isValid(value)) {
            return null;
        }
        return new ObjectId(value);
    }

    private String clean(String value) {
        return value == null ? "" : value.trim();
    }

    private void showError(
            HttpServletRequest request,
            HttpServletResponse response,
            Document article,
            String message
    ) throws ServletException, IOException {

        response.setStatus(400);

        request.setAttribute("article", article);
        request.setAttribute("error", message);

        request.setAttribute(
                "initialContentHtml",
                RichTextUtil.sanitize(
                        article.getString("contentHtml"))
        );

        request.getRequestDispatcher(VIEW)
                .forward(request, response);
    }
}
