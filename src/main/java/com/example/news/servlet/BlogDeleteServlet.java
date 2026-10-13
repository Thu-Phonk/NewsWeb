
package com.example.news.servlet;

import com.example.news.user.UserCollection;
import com.example.news.util.BlogCollection;

import com.mongodb.client.model.Filters;

import org.bson.Document;
import org.bson.types.ObjectId;

import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.util.Set;

@WebServlet("/blog/delete")
public class BlogDeleteServlet extends HttpServlet {

    private static final Set<String> ALLOWED_ROLES =
            Set.of("guest", "subscriber", "writer",
                   "editor", "administrator");

    @Override
    protected void doPost(
            HttpServletRequest request,
            HttpServletResponse response
    ) throws IOException {

        response.setHeader("Cache-Control", "no-store");

        // Step 1: Require an authenticated session.
        HttpSession session = request.getSession(false);

        if (session == null ||
                !(session.getAttribute("userId") instanceof String userId)
                || !ObjectId.isValid(userId)) {

            response.sendError(401, "Login required.");
            return;
        }

        // Step 2: Verify CSRF protection.
        Object storedToken =
                session.getAttribute("blogDeleteCsrfToken");

        String submittedToken =
                request.getParameter("csrfToken");

        if (!(storedToken instanceof String expectedToken)
                || submittedToken == null
                || !MessageDigest.isEqual(
                        expectedToken.getBytes(StandardCharsets.UTF_8),
                        submittedToken.getBytes(StandardCharsets.UTF_8))) {

            response.sendError(403, "Invalid form token.");
            return;
        }

        // Step 3: Validate the blog ID.
        String blogId = request.getParameter("id");

        if (blogId == null || !ObjectId.isValid(blogId)) {
            response.sendError(400, "Invalid blog ID.");
            return;
        }

        try {
            ObjectId authorId = new ObjectId(userId);

            // Step 4: Check the current account and role.
            Document user = UserCollection.getUsers()
                    .find(Filters.eq("_id", authorId))
                    .first();

            if (user == null ||
                    !ALLOWED_ROLES.contains(user.getString("role"))) {

                response.sendError(403, "Access denied.");
                return;
            }

            // Step 5: Delete only a blog owned by this user.
            var result = BlogCollection.getCollection()
                    .deleteOne(
                            Filters.and(
                                    Filters.eq(
                                            "_id", new ObjectId(blogId)
                                    ),
                                    Filters.eq(
                                            "authorId", authorId
                                    )
                            )
                    );

            if (result.getDeletedCount() != 1) {
                response.sendError(404, "Blog not found.");
                return;
            }

            // Step 6: Return to My Posts.
            response.sendRedirect(
                    request.getContextPath()
                    + "/blog/my-posts?deleted=1"
            );

        } catch (Exception e) {
            getServletContext().log(
                    "Blog deletion failed.", e
            );

            response.sendError(
                    500, "Unable to delete blog."
            );
        }
    }
}
