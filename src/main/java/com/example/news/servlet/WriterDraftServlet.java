package com.example.news.servlet;

import com.example.news.util.EditorialCollection;
import com.example.news.util.RoleUtil;
import com.example.news.util.RichTextUtil;
import com.example.news.util.CloudinaryImageUtil;

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

@WebServlet("/writer/draft")
@MultipartConfig(fileSizeThreshold = 0, maxFileSize = 5L * 1024 * 1024,
        maxRequestSize = 6L * 1024 * 1024)
public class WriterDraftServlet extends HttpServlet {

    private static final String TOKEN_KEY = "writerDraftCsrfToken";
    private static final String VIEW =
            "/WEB-INF/views/writer-draft.jsp";

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

            ObjectId articleId = parseId(request.getParameter("id"));

            if (articleId == null) {
                response.sendError(400, "Invalid article ID.");
                return;
            }

            // Only load a draft belonging to this writer.
            Document draft = EditorialCollection.getCollection()
                    .find(Filters.and(
                            Filters.eq("_id", articleId),
                            Filters.eq("authorId", user.getObjectId("_id")),       
                            Filters.eq("status", "draft")
                    ))
                    .first();

            if (draft == null) {
                response.sendError(
                        404, "Draft not found or not editable."
                );
                return;
            }

            HttpSession session = request.getSession();

            if (session.getAttribute(TOKEN_KEY) == null) {
                session.setAttribute(
                        TOKEN_KEY, UUID.randomUUID().toString()
                );
            }

            request.setAttribute("draft", draft);
request.setAttribute(
        "initialContentHtml",
        RichTextUtil.sanitize(draft.getString("contentHtml"))
);
            request.getRequestDispatcher(VIEW)
                    .forward(request, response);

        } catch (Exception e) {
            getServletContext().log("Unable to load draft.", e);
            response.sendError(500, "Unable to load draft.");
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
            String submitted = request.getParameter("csrfToken");

            if (expected == null || submitted == null ||
                    !MessageDigest.isEqual(
                            expected.getBytes(StandardCharsets.UTF_8),
                            submitted.getBytes(StandardCharsets.UTF_8))) {

                response.sendError(403, "Invalid form token.");
                return;
            }

            ObjectId articleId = parseId(request.getParameter("id"));

            if (articleId == null) {
                response.sendError(400, "Invalid article ID.");
                return;
            }

            // Load the complete persisted draft so failed form validation
            // does not lose its existing cover image.
            Document storedDraft = EditorialCollection.getCollection()
                    .find(Filters.and(
                            Filters.eq("_id", articleId),
                            Filters.eq("authorId", user.getObjectId("_id")),
                            Filters.eq("status", "draft")))
                    .first();

            if (storedDraft == null) {
                response.sendError(404, "Draft no longer editable.");
                return;
            }

            String action = request.getParameter("action");

            boolean saveDraft = "draft".equals(action);

            if (!saveDraft && !"submit".equals(action)) {
                response.sendError(400, "Invalid article action.");
                return;
            }

            String title = clean(request.getParameter("title"));
            String category = clean(request.getParameter("category"));
            String content = clean(request.getParameter("content"));

String rawHtml = request.getParameter("contentHtml");
String safeHtml = null;

if (rawHtml != null && !rawHtml.isBlank()) {

    if (rawHtml.length() > 100000) {
        response.sendError(
                400, "Formatted article content is too large."
        );
        return;
    }

    safeHtml = RichTextUtil.sanitize(rawHtml);
    content = RichTextUtil.toPlainText(safeHtml);
}

            // Preserve entered data if validation fails.
            Document formDraft = new Document(storedDraft)
                    .append("title", title)
                    .append("category", category)
                    .append("content", content);
if (safeHtml != null) {
    formDraft.append("contentHtml", safeHtml);
}
            if (title.length() > 200 ||
                    category.length() > 60 ||
                    content.length() > 20000) {

                showError(request, response, formDraft,
                        "One or more fields are too long.");
                return;
            }

            if (saveDraft) {
                if (title.isEmpty() &&
                        category.isEmpty() &&
                        content.isEmpty()) {

                    showError(request, response, formDraft,
                            "A draft cannot be completely empty.");
                    return;
                }
            } else {
                if (title.length() < 5 ||
                        category.length() < 2 ||
                        content.length() < 50) {

                    showError(request, response, formDraft,
                            "To submit: title needs 5 characters, " +
                            "category needs 2, and content needs 50.");
                    return;
                }
            }

            String nextStatus = saveDraft ? "draft" : "pending";

// Update formatted HTML when Quill is available.
// Clear old formatting if the user saved plain text.
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

            String previousCoverId = storedDraft.getString("coverImagePublicId");
            List<Bson> updates = new ArrayList<>();
            updates.add(Updates.set("title", title));
            updates.add(Updates.set("category", category));
            updates.add(Updates.set("content", content));
            updates.add(htmlUpdate);
            updates.add(Updates.set("status", nextStatus));
            updates.add(Updates.set("updatedAt", new Date()));
            if (removeCover) {
                updates.add(Updates.unset("coverImageUrl"));
                updates.add(Updates.unset("coverImagePublicId"));
            } else if (newCover != null) {
                updates.add(Updates.set("coverImageUrl", newCover.url()));
                updates.add(Updates.set("coverImagePublicId", newCover.publicId()));
            }

            // Atomic ownership and status check:
            // cannot edit another author's or submitted article.
            long matched = EditorialCollection.getCollection()
                    .updateOne(
                            Filters.and(
                                    Filters.eq("_id", articleId),
                                    Filters.eq(
                                            "authorId",
                                            user.getObjectId("_id")
                                    ),
                                    Filters.eq("status", "draft"),
                                    Filters.eq("updatedAt", storedDraft.getDate("updatedAt"))
                            ),

                            Updates.combine(updates)

                    )
                    .getMatchedCount();

            if (matched == 0) {
                response.sendError(
                        404, "Draft not found or no longer editable."
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
                    request.getContextPath() +
                    (saveDraft
                            ? "/writer/articles?updated=draft"
                            : "/writer/articles?submitted=1")
            );

        } catch (Exception e) {
            getServletContext().log("Unable to update draft.", e);
            response.sendError(500, "Unable to update draft.");
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

            response.sendError(403, "Editorial access denied.");
            return null;
        }

        return user;
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
            Document draft,
            String message
    ) throws ServletException, IOException {

        response.setStatus(400);
        request.setAttribute("draft", draft);
        request.setAttribute("error", message);
request.setAttribute(
        "initialContentHtml",
        RichTextUtil.sanitize(draft.getString("contentHtml"))
);
        request.getRequestDispatcher(VIEW)
                .forward(request, response);
    }
}
