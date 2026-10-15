
package com.example.news.servlet;

import com.example.news.user.UserCollection;
import com.example.news.util.BlogCollection;
import com.example.news.util.RichTextUtil;

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

@WebServlet("/blog/my-posts")
public class MyBlogServlet extends HttpServlet {

    @Override
    protected void doGet(
            HttpServletRequest request,
            HttpServletResponse response
    ) throws ServletException, IOException {

        response.setHeader("Cache-Control", "no-store");

        HttpSession session = request.getSession(false);

        if (session == null ||
                !(session.getAttribute("userId") instanceof String userId)) {

            response.sendRedirect(
                    request.getContextPath() + "/login"
            );
            return;
        }

        if (!ObjectId.isValid(userId)) {
            response.sendError(403, "Invalid session.");
            return;
        }

        try {
            ObjectId authorId = new ObjectId(userId);

            // Confirm that the account still exists.
            Document user = UserCollection.getUsers()
                    .find(Filters.eq("_id", authorId))
                    .first();

            if (user == null) {
                session.invalidate();
                response.sendRedirect(
                        request.getContextPath() + "/login"
                );
                return;
            }

            
List<Document> myBlogs = new ArrayList<>();

// Only return blogs owned by this user.
for (Document blog : BlogCollection.getCollection()
        .find(Filters.eq("authorId", authorId))
        .sort(Sorts.descending("createdAt"))
        .limit(50)) {

    // Retrieve formatted HTML saved in MongoDB.
    String storedHtml = blog.getString("contentHtml");

    if (storedHtml != null && !storedHtml.isBlank()) {

        // Sanitize HTML before displaying it.
        String safeHtml =
                RichTextUtil.sanitize(storedHtml);

        // Store the sanitized version for the JSP.
        blog.put("safeContentHtml", safeHtml);
    }

    myBlogs.add(blog);
}

// Send the list to my-posts.jsp.
request.setAttribute("myBlogs", myBlogs);

// Generate a CSRF token for deleting blog posts.
if (session.getAttribute("blogDeleteCsrfToken") == null) {
    session.setAttribute(
            "blogDeleteCsrfToken",
            UUID.randomUUID().toString()
    );
}
            request.getRequestDispatcher(
                    "/WEB-INF/views/my-posts.jsp"
            ).forward(request, response);

        } catch (Exception e) {
            getServletContext().log(
                    "Unable to load user's blogs.", e
            );

            response.sendError(
                    500, "Unable to load your blog posts."
            );
        }
    }
}
