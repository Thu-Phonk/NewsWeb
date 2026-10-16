
package com.example.news.servlet;

import com.example.news.user.UserCollection;
import com.example.news.util.BlogCollection;
import com.mongodb.client.model.Filters;
import com.mongodb.client.model.Updates;
import com.example.news.util.RichTextUtil;
import com.example.news.util.CloudinaryImageUtil;

import org.bson.Document;
import org.bson.types.ObjectId;
import org.bson.conversions.Bson;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import jakarta.servlet.annotation.MultipartConfig;
import jakarta.servlet.http.Part;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.util.Date;
import java.util.Set;
import java.util.UUID;
import java.util.ArrayList;
import java.util.List;


@WebServlet("/blog/edit")
@MultipartConfig(
    fileSizeThreshold = 0,
    maxFileSize = 5L * 1024 * 1024,
    maxRequestSize = 6L * 1024 * 1024
)
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

request.setAttribute(
        "initialContentHtml",
        RichTextUtil.sanitize(blog.getString("contentHtml"))
);

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

    String expected =
            (String) session.getAttribute("blogEditCsrfToken");

    String submitted = request.getParameter("csrfToken");

    if (expected == null || submitted == null ||
            !MessageDigest.isEqual(
                    expected.getBytes(StandardCharsets.UTF_8),
                    submitted.getBytes(StandardCharsets.UTF_8))) {

        response.sendError(403, "Invalid form token.");
        return;
    }

    String id = request.getParameter("id");

    if (id == null || !ObjectId.isValid(id)) {
        response.sendError(400, "Invalid blog ID.");
        return;
    }

    String action = request.getParameter("action");
    boolean saveDraft = "draft".equals(action);

    if (!saveDraft && !"submit".equals(action)) {
        response.sendError(400, "Invalid blog action.");
        return;
    }

    CloudinaryImageUtil.UploadedImage newCover = null;
    boolean databaseSaved = false;

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

        String currentStatus = blog.getString("status");

        if (!"draft".equals(currentStatus)
                && !"pending".equals(currentStatus)
                && !"rejected".equals(currentStatus)
                && !"published".equals(currentStatus)) {

            response.sendError(409, "Unsupported blog status.");
            return;
        }

        if (saveDraft && !"draft".equals(currentStatus)) {
            response.sendError(
                    409, "Only drafts can be saved as drafts."
            );
            return;
        }

        // Preserve current Cloudinary information.
        String oldCoverId =
                blog.getString("coverImagePublicId");

        String oldCoverUrl =
                blog.getString("coverImageUrl");

        String title = request.getParameter("title");
        String category = request.getParameter("category");
        String content = request.getParameter("content");
        String rawHtml = request.getParameter("contentHtml");

        title = title == null ? "" : title.trim();
        category = category == null ? "" : category.trim();
        content = content == null ? "" : content.trim();

        String safeHtml = null;

        if (rawHtml != null && !rawHtml.isBlank()) {

            if (rawHtml.length() > 50000) {
                response.sendError(
                        400, "Formatted content is too large."
                );
                return;
            }

            safeHtml = RichTextUtil.sanitize(rawHtml);
            content = RichTextUtil.toPlainText(safeHtml);
        }

        // Restore the user's input on validation errors.
        Document formBlog = new Document(blog);
        formBlog.put("title", title);
        formBlog.put("category", category);
        formBlog.put("content", content);
        formBlog.put("contentHtml", safeHtml);

        if (title.length() > 150
                || category.length() > 60
                || content.length() > 10000) {

            showError(request, response, formBlog,
                    "One or more fields are too long.");
            return;
        }

        if (saveDraft) {

            if (title.isEmpty()
                    && category.isEmpty()
                    && content.isEmpty()) {

                showError(request, response, formBlog,
                        "A draft cannot be completely empty.");
                return;
            }

        } else {

            if (title.length() < 3
                    || category.length() < 2
                    || content.length() < 10) {

                showError(request, response, formBlog,
                        "Title needs 3 characters, category "
                        + "needs 2, and content needs 10.");
                return;
            }
        }

        // Determine what to do with the cover image.
        boolean removeCover =
                "true".equals(request.getParameter("removeCover"));

        Part imagePart;

        try {
            imagePart = request.getPart("coverImage");
        } catch (IllegalStateException e) {
            response.sendError(413, "Uploaded file is too large.");
            return;
        }

        boolean hasNewImage =
                imagePart != null && imagePart.getSize() > 0;

        // Never process contradictory instructions.
        if (removeCover && hasNewImage) {
            showError(
                    request, response, formBlog,
                    "Choose either Replace or Remove Cover."
            );
            return;
        }

        // Upload new image only after validation has passed.
        if (hasNewImage) {
            try {
                newCover =
                        CloudinaryImageUtil.uploadCover(imagePart);

            } catch (IllegalArgumentException e) {
                showError(request, response, formBlog,
                        e.getMessage());
                return;
            }
        }

        List<Bson> updates = new ArrayList<>();

        updates.add(Updates.set("title", title));
        updates.add(Updates.set("category", category));
        updates.add(Updates.set("content", content));

        if (safeHtml == null) {
            updates.add(Updates.unset("contentHtml"));
        } else {
            updates.add(Updates.set("contentHtml", safeHtml));
        }

        // CASE 1: Remove the current image.
        if (removeCover) {
            updates.add(Updates.unset("coverImageUrl"));
            updates.add(Updates.unset("coverImagePublicId"));
        }

        // CASE 2: Replace with a newly uploaded image.
        else if (newCover != null) {
            updates.add(
                    Updates.set("coverImageUrl", newCover.url())
            );
            updates.add(
                    Updates.set(
                            "coverImagePublicId",
                            newCover.publicId()
                    )
            );
        }

        // CASE 3: Neither selected.
        // No image update is added, so the old cover remains.

        String nextStatus = saveDraft ? "draft" : "pending";

        updates.add(Updates.set("status", nextStatus));
        updates.add(Updates.set("updatedAt", new Date()));
        updates.add(Updates.unset("reviewedBy"));
        updates.add(Updates.unset("reviewedAt"));
        updates.add(Updates.unset("rejectionReason"));

        long matched = BlogCollection.getCollection()
                .updateOne(
                        Filters.and(
                                Filters.eq("_id", blogId),
                                Filters.eq("authorId", authorId),
                                Filters.eq("status", currentStatus),
                                Filters.eq(
                                        "updatedAt",
                                        blog.getDate("updatedAt")
                                )
                        ),
                        Updates.combine(updates)
                )
                .getMatchedCount();

        if (matched != 1) {
            response.sendError(
                    409, "Blog changed while you were editing."
            );
            return;
        }

        databaseSaved = true;

        // The database now points to the correct image.
        // Deleting the old image cannot affect the new cover.
        if ((removeCover || newCover != null)
                && oldCoverId != null
                && !oldCoverId.isBlank()) {

            try {
                CloudinaryImageUtil.deleteCover(oldCoverId);

            } catch (IOException e) {
                getServletContext().log(
                        "Old cover cleanup failed: " + oldCoverId,
                        e
                );
            }
        }

        session.removeAttribute("blogEditCsrfToken");

        response.sendRedirect(
                request.getContextPath()
                + (saveDraft
                    ? "/blog/my-posts?saved=draft"
                    : "/blog/my-posts?updated=1")
        );

    } catch (Exception e) {
        getServletContext().log("Blog editing failed.", e);

        if (!response.isCommitted()) {
            response.sendError(500, "Unable to update blog.");
        }

    } finally {

        // A successful Cloudinary upload followed by a failed
        // database update must not leave an unused new image.
        if (!databaseSaved && newCover != null) {

            try {
                CloudinaryImageUtil.deleteCover(
                        newCover.publicId()
                );

            } catch (IOException e) {
                getServletContext().log(
                        "Failed to clean unused uploaded cover.",
                        e
                );
            }
        }
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
request.setAttribute(
        "initialContentHtml",
        RichTextUtil.sanitize(blog.getString("contentHtml"))
);
        request.getRequestDispatcher(
                "/WEB-INF/views/blog-edit.jsp"
        ).forward(request, response);
    }
}
