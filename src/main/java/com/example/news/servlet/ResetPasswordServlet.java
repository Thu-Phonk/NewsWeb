
package com.example.news.servlet;

import com.example.news.user.UserCollection;
import com.example.news.util.PasswordResetTokenUtil;

import com.mongodb.client.model.Filters;
import com.mongodb.client.model.Updates;

import at.favre.lib.crypto.bcrypt.BCrypt;

import org.bson.Document;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.Date;

@WebServlet("/reset")
public class ResetPasswordServlet extends HttpServlet {

    @Override
    protected void doGet(
            HttpServletRequest request,
            HttpServletResponse response
    ) throws ServletException, IOException {

        setSecurityHeaders(response);

        String token = request.getParameter("token");

        if (!isValidTokenFormat(token)) {
            showInvalidToken(request, response);
            return;
        }

        try {
            String tokenHash =
                    PasswordResetTokenUtil.hashToken(token);

            Document user = UserCollection.getUsers()
                    .find(Filters.and(
                            Filters.eq("resetTokenHash", tokenHash),
                            Filters.gt("resetExpiresAt", new Date())
                    ))
                    .first();

            if (user == null) {
                showInvalidToken(request, response);
                return;
            }

            request.setAttribute("token", token);

            request.getRequestDispatcher(
                    "/WEB-INF/views/reset.jsp"
            ).forward(request, response);

        } catch (Exception e) {
            getServletContext().log(
                    "Reset token verification failed.", e
            );
            response.sendError(500, "Unable to verify reset link.");
        }
    }

    @Override
    protected void doPost(
            HttpServletRequest request,
            HttpServletResponse response
    ) throws ServletException, IOException {

        setSecurityHeaders(response);
        request.setCharacterEncoding("UTF-8");

        String token = request.getParameter("token");
        String password = request.getParameter("password");
        String confirm = request.getParameter("confirm_password");

        if (!isValidTokenFormat(token)) {
            showInvalidToken(request, response);
            return;
        }

        if (password == null || confirm == null) {
            showError(request, response, token,
                    "All fields are required.");
            return;
        }

        int passwordBytes =
                password.getBytes(StandardCharsets.UTF_8).length;

        if (passwordBytes < 8 || passwordBytes > 72) {
            showError(request, response, token,
                    "Password must be between 8 and 72 UTF-8 bytes.");
            return;
        }

        if (!password.equals(confirm)) {
            showError(request, response, token,
                    "Passwords do not match.");
            return;
        }

        try {
            String tokenHash =
                    PasswordResetTokenUtil.hashToken(token);

            String passwordHash = BCrypt.withDefaults()
                    .hashToString(12, password.toCharArray());

            // The token must still exist and be unexpired
            // when the password update happens.
            var result = UserCollection.getUsers().updateOne(
                    Filters.and(
                            Filters.eq("resetTokenHash", tokenHash),
                            Filters.gt("resetExpiresAt", new Date())
                    ),
                    Updates.combine(
                            Updates.set("password", passwordHash),
                            Updates.set("passwordChangedAt", new Date()),
                            Updates.unset("resetTokenHash"),
                            Updates.unset("resetExpiresAt")
                    )
            );

            if (result.getModifiedCount() != 1) {
                showInvalidToken(request, response);
                return;
            }

            // End the current browser's session, if any.
            var session = request.getSession(false);

            if (session != null) {
                session.invalidate();
            }

            response.sendRedirect(
                    request.getContextPath()
                    + "/login?reset=success"
            );

        } catch (Exception e) {
            getServletContext().log(
                    "Password reset failed.", e
            );

            response.sendError(
                    500, "Unable to reset password."
            );
        }
    }

    private boolean isValidTokenFormat(String token) {
        return token != null
                && token.matches("[0-9a-f]{64}");
    }

    private void showInvalidToken(
            HttpServletRequest request,
            HttpServletResponse response
    ) throws ServletException, IOException {

        response.setStatus(400);
        request.setAttribute("invalidToken", true);

        request.getRequestDispatcher(
                "/WEB-INF/views/reset.jsp"
        ).forward(request, response);
    }

    private void showError(
            HttpServletRequest request,
            HttpServletResponse response,
            String token,
            String error
    ) throws ServletException, IOException {

        response.setStatus(400);
        request.setAttribute("token", token);
        request.setAttribute("error", error);

        request.getRequestDispatcher(
                "/WEB-INF/views/reset.jsp"
        ).forward(request, response);
    }

    private void setSecurityHeaders(
            HttpServletResponse response
    ) {

        response.setHeader("Cache-Control", "no-store");
        response.setHeader("Referrer-Policy", "no-referrer");
        response.setHeader("X-Content-Type-Options", "nosniff");
    }
}
