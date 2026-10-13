
package com.example.news.servlet;

import com.example.news.user.UserCollection;
import com.mongodb.client.model.Filters;
import com.mongodb.client.model.Projections;
import at.favre.lib.crypto.bcrypt.BCrypt;

import org.bson.Document;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;

import java.io.IOException;
import java.util.Locale;

@WebServlet("/login")
public class LoginServlet extends HttpServlet {

    @Override
    protected void doGet(
            HttpServletRequest request,
            HttpServletResponse response
    ) throws ServletException, IOException {

        response.setHeader("Cache-Control", "no-store");

        request.getRequestDispatcher(
                "/WEB-INF/views/login.jsp"
        ).forward(request, response);
    }

    @Override
    protected void doPost(
            HttpServletRequest request,
            HttpServletResponse response
    ) throws ServletException, IOException {

        request.setCharacterEncoding("UTF-8");

        String loginName = request.getParameter("username");
        String password = request.getParameter("password");

        if (loginName == null || password == null) {
            showError(request, response);
            return;
        }

        loginName = loginName.trim();

        if (loginName.isEmpty()
                || loginName.length() > 254
                || password.isEmpty()
                || password.length() > 1024) {

            showError(request, response);
            return;
        }

        try {
            Document user = UserCollection.getUsers()
                    .find(Filters.or(
                            Filters.eq("username", loginName),
                            Filters.eq(
                                    "email",
                                    loginName.toLowerCase(Locale.ROOT)
                            )
                    ))
                    .projection(Projections.include(
                            "_id", "username", "password", "role"
                    ))
                    .first();

            if (user == null) {
                showError(request, response);
                return;
            }

            String storedHash = user.getString("password");

            if (storedHash == null) {
                showError(request, response);
                return;
            }

            boolean validPassword = BCrypt.verifyer()
                    .verify(password.toCharArray(), storedHash)
                    .verified;

            if (!validPassword) {
                showError(request, response);
                return;
            }

            // Remove the old session to prevent session fixation.
            HttpSession oldSession = request.getSession(false);

            if (oldSession != null) {
                oldSession.invalidate();
            }

            HttpSession session = request.getSession(true);

            session.setAttribute(
                    "userId",
                    user.getObjectId("_id").toHexString()
            );

            session.setAttribute(
                    "username",
                    user.getString("username")
            );

            session.setAttribute(
                    "role",
                    user.getString("role")
            );

            session.setMaxInactiveInterval(30 * 60);

            response.sendRedirect(
                    request.getContextPath() + "/login"
            );

        } catch (Exception e) {
            getServletContext().log("Login failed.", e);

            response.sendError(
                    HttpServletResponse.SC_INTERNAL_SERVER_ERROR,
                    "An unexpected error occurred."
            );
        }
    }

    private void showError(
            HttpServletRequest request,
            HttpServletResponse response
    ) throws ServletException, IOException {

        response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
        response.setHeader("Cache-Control", "no-store");

        request.setAttribute(
                "error",
                "Invalid username/email or password."
        );

        request.getRequestDispatcher(
                "/WEB-INF/views/login.jsp"
        ).forward(request, response);
    }
}
