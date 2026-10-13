
package com.example.news.servlet;

import com.example.news.user.UserCollection;
import com.mongodb.MongoWriteException;
import com.mongodb.client.model.Filters;
import at.favre.lib.crypto.bcrypt.BCrypt;

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
import java.security.SecureRandom;
import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.Base64;
import java.util.Date;
import java.util.Locale;

@WebServlet("/register")
public class RegisterServlet extends HttpServlet {

    private static final SecureRandom RANDOM =
            new SecureRandom();

    @Override
    protected void doGet(
            HttpServletRequest request,
            HttpServletResponse response
    ) throws ServletException, IOException {

        showForm(request, response, null);
    }

    @Override
    protected void doPost(
            HttpServletRequest request,
            HttpServletResponse response
    ) throws ServletException, IOException {

        request.setCharacterEncoding("UTF-8");

        // Verify the form came from our application.
        HttpSession session = request.getSession(false);

        String expected = session == null ? null :
                (String) session.getAttribute("registerCsrfToken");

        String submitted = request.getParameter("csrfToken");

        if (expected == null || submitted == null ||
                !MessageDigest.isEqual(
                    expected.getBytes(StandardCharsets.UTF_8),
                    submitted.getBytes(StandardCharsets.UTF_8))) {

            response.sendError(
                    HttpServletResponse.SC_FORBIDDEN,
                    "Invalid registration request."
            );
            return;
        }

        session.removeAttribute("registerCsrfToken");

        String username = trim(request.getParameter("username"));
        String email = trim(request.getParameter("email"))
                .toLowerCase(Locale.ROOT);
        String fullName = trim(request.getParameter("fullName"));
        String password = request.getParameter("password");
        String confirm = request.getParameter("confirmPassword");

        if (!username.matches("[A-Za-z0-9_]{3,30}")) {
            showForm(request, response,
                    "Username must be 3-30 letters, numbers or underscores.");
            return;
        }

        if (email.length() > 254 ||
                !email.matches("^[^\\s@]+@[^\\s@]+\\.[^\\s@]+$")) {
            showForm(request, response, "Invalid email address.");
            return;
        }

        if (fullName.isEmpty() || fullName.length() > 100) {
            showForm(request, response,
                    "Full name must be between 1 and 100 characters.");
            return;
        }

        if (password == null ||
                password.length() < 8 ||
                password.getBytes(StandardCharsets.UTF_8).length > 72) {

            showForm(request, response,
                    "Password must be at least 8 characters and at most 72 UTF-8 bytes.");
            return;
        }

        if (!password.equals(confirm)) {
            showForm(request, response, "Passwords do not match.");
            return;
        }

        try {
            // Prevent creating an account with an existing username/email.
            Document existing = UserCollection.getUsers()
                    .find(Filters.or(
                            Filters.eq("username", username),
                            Filters.eq("email", email)
                    ))
                    .first();

            if (existing != null) {
                showForm(request, response,
                        "Username or email is already registered.");
                return;
            }

            String hashedPassword = BCrypt.withDefaults()
                    .hashToString(12, password.toCharArray());

            Date expiry = Date.from(
                    ZonedDateTime.now(ZoneOffset.UTC)
                            .plusYears(1)
                            .toInstant()
            );

            Document newUser = new Document()
                    .append("username", username)
                    .append("email", email)
                    .append("full_name", fullName)
                    .append("password", hashedPassword)
                    .append("role", "guest")
                    .append("subscription_expiration", expiry)
                    .append("avaiCategory", new ArrayList<>())
                    .append("savedArticles", new ArrayList<>());

            UserCollection.getUsers().insertOne(newUser);

            response.sendRedirect(
                    request.getContextPath() + "/login"
            );

        } catch (MongoWriteException e) {

            if (e.getCode() == 11000) {
                showForm(request, response,
                        "Username or email is already registered.");
                return;
            }

            getServletContext().log("Registration failed.", e);
            response.sendError(500, "Registration failed.");

        } catch (Exception e) {

            getServletContext().log("Registration failed.", e);
            response.sendError(500, "Registration failed.");
        }
    }

    private static String trim(String value) {
        return value == null ? "" : value.trim();
    }

    private void showForm(
            HttpServletRequest request,
            HttpServletResponse response,
            String error
    ) throws ServletException, IOException {

        byte[] bytes = new byte[32];
        RANDOM.nextBytes(bytes);

        String token = Base64.getUrlEncoder()
                .withoutPadding()
                .encodeToString(bytes);

        request.getSession(true).setAttribute(
                "registerCsrfToken", token
        );

        response.setHeader("Cache-Control", "no-store");

        if (error != null) {
            request.setAttribute("error", error);
            response.setStatus(400);
        }

        request.getRequestDispatcher(
                "/WEB-INF/views/register.jsp"
        ).forward(request, response);
    }
}
