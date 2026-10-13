
package com.example.news.servlet;

import com.example.news.util.EditorialCollection;
import com.example.news.util.RoleUtil;

import org.bson.Document;

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
import java.util.UUID;

@WebServlet("/writer/submit")
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

        try {
            if (!checkAccess(request, response)) {
                return;
            }

            HttpSession session = request.getSession(false);

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

            if (title == null || category == null ||
                    content == null) {

                showError(request, response,
                        "All fields are required.");
                return;
            }

            title = title.trim();
            category = category.trim();
            content = content.trim();

            if (title.length() < 5 || title.length() > 200) {
                showError(request, response,
                        "Title must be 5 to 200 characters.");
                return;
            }

            if (category.length() < 2 ||
                    category.length() > 60) {

                showError(request, response,
                        "Category must be 2 to 60 characters.");
                return;
            }

            if (content.length() < 50 ||
                    content.length() > 20000) {

                showError(request, response,
                        "Content must be 50 to 20000 characters.");
                return;
            }

            // Retrieve identity and current role from MongoDB.
            Document user = RoleUtil.getCurrentUser(request);

            if (!RoleUtil.hasRole(
                    user, "writer", "editor", "administrator")) {

                response.sendError(403, "Access denied.");
                return;
            }

            Date now = new Date();

            Document article = new Document()
                    .append("title", title)
                    .append("category", category)
                    .append("content", content)
                    .append("authorId", user.getObjectId("_id"))
                    .append("authorName", user.getString("username"))
                    .append("status", "pending")
                    .append("createdAt", now)
                    .append("updatedAt", now);

            EditorialCollection.getCollection()
                    .insertOne(article);

            session.removeAttribute("articleCsrfToken");

            response.sendRedirect(
                    request.getContextPath()
                    + "/writer/submit?submitted=1"
            );

        } catch (Exception e) {
            getServletContext().log(
                    "Article submission failed.", e
            );
            response.sendError(
                    500, "Unable to submit article."
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

        request.getRequestDispatcher(
                "/WEB-INF/views/writer-submit.jsp"
        ).forward(request, response);
    }
}
