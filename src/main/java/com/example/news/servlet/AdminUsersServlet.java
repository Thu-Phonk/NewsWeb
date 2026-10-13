
package com.example.news.servlet;

import com.example.news.user.UserCollection;
import com.example.news.util.RoleUtil;

import com.mongodb.client.model.Filters;
import com.mongodb.client.model.Projections;
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
import java.util.List;
import java.util.Set;
import java.util.UUID;

@WebServlet("/admin/users")
public class AdminUsersServlet extends HttpServlet {

    private static final Set<String> VALID_ROLES =
            Set.of(
                "guest",
                "subscriber",
                "writer",
                "editor",
                "administrator"
            );

    @Override
    protected void doGet(
            HttpServletRequest request,
            HttpServletResponse response
    ) throws ServletException, IOException {

        response.setHeader("Cache-Control", "no-store");

        try {
            Document admin = RoleUtil.getCurrentUser(request);

            if (admin == null) {
                response.sendRedirect(
                    request.getContextPath() + "/login"
                );
                return;
            }

            if (!RoleUtil.hasRole(admin, "administrator")) {
                response.sendError(
                    403, "Administrator access required."
                );
                return;
            }

            HttpSession session = request.getSession(false);

            if (session.getAttribute("adminCsrfToken") == null) {
                session.setAttribute(
                    "adminCsrfToken",
                    UUID.randomUUID().toString()
                );
            }

            List<Document> users = new ArrayList<>();

            // Exclude passwords and password-reset data.
            for (Document user : UserCollection.getUsers()
                    .find()
                    .projection(Projections.include(
                        "_id", "username", "email",
                        "full_name", "role"
                    ))
                    .sort(Sorts.ascending("username"))
                    .limit(100)) {

                users.add(user);
            }

            request.setAttribute("users", users);
	    request.setAttribute(
    "adminUsername", admin.getString("username")
);
            request.setAttribute(
                "adminId", admin.getObjectId("_id").toHexString()
            );

            request.getRequestDispatcher(
                "/WEB-INF/views/admin-users.jsp"
            ).forward(request, response);

        } catch (Exception e) {
            getServletContext().log(
                "Unable to load user management.", e
            );
            response.sendError(500);
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
            Document admin = RoleUtil.getCurrentUser(request);

            if (admin == null ||
                    !RoleUtil.hasRole(admin, "administrator")) {
                response.sendError(
                    403, "Administrator access required."
                );
                return;
            }

            HttpSession session = request.getSession(false);

            if (session == null) {
                response.sendError(403);
                return;
            }

            Object savedToken =
                    session.getAttribute("adminCsrfToken");

            String submittedToken =
                    request.getParameter("csrfToken");

            if (!(savedToken instanceof String expected)
                    || submittedToken == null
                    || !MessageDigest.isEqual(
                        expected.getBytes(StandardCharsets.UTF_8),
                        submittedToken.getBytes(StandardCharsets.UTF_8))) {

                response.sendError(
                    403, "Invalid form token."
                );
                return;
            }

            String targetId = request.getParameter("userId");
            String newRole = request.getParameter("role");

            if (targetId == null ||
                    !ObjectId.isValid(targetId) ||
                    newRole == null ||
                    !VALID_ROLES.contains(newRole)) {

                response.sendError(
                    400, "Invalid user or role."
                );
                return;
            }

            ObjectId adminId = admin.getObjectId("_id");
            ObjectId userId = new ObjectId(targetId);

            // Prevent administrators from changing
            // their own permissions.
            if (adminId.equals(userId)) {
                response.sendError(
                    403, "You cannot change your own role."
                );
                return;
            }

            var result = UserCollection.getUsers()
                    .updateOne(
                        Filters.and(
                            Filters.eq("_id", userId),
                            Filters.ne("_id", adminId)
                        ),
                        Updates.set("role", newRole)
                    );

            if (result.getMatchedCount() != 1) {
                response.sendError(
                    404, "User not found."
                );
                return;
            }

            response.sendRedirect(
                request.getContextPath()
                + "/admin/users?updated=1"
            );

        } catch (Exception e) {
            getServletContext().log(
                "User role update failed.", e
            );

            response.sendError(
                500, "Unable to update user role."
            );
        }
    }
}
