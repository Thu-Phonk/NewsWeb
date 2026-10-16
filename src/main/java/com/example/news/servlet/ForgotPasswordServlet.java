
package com.example.news.servlet;

import com.example.news.user.UserCollection;
import com.example.news.util.PasswordResetTokenUtil;

import com.mongodb.client.model.Filters;
import com.mongodb.client.model.Updates;
import com.mongodb.client.model.FindOneAndUpdateOptions;
import com.mongodb.client.model.ReturnDocument;

import com.sendgrid.SendGrid;
import com.sendgrid.Request;
import com.sendgrid.Response;
import com.sendgrid.Method;
import com.sendgrid.helpers.mail.Mail;
import com.sendgrid.helpers.mail.objects.Email;
import com.sendgrid.helpers.mail.objects.Content;

import org.bson.Document;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;
import java.security.SecureRandom;
import java.security.MessageDigest;
import java.nio.charset.StandardCharsets;
import java.util.Base64;
import jakarta.servlet.http.HttpSession;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Date;
import java.util.Locale;

@WebServlet("/forgot")
public class ForgotPasswordServlet extends HttpServlet {
    private static final SecureRandom CSRF_RANDOM = new SecureRandom();
    private static final String CSRF_SESSION_KEY = "forgotPasswordCsrfToken";

    private void prepareFormToken(HttpServletRequest request) {
        HttpSession session = request.getSession(true);
        if (session.getAttribute(CSRF_SESSION_KEY) == null) {
            byte[] bytes = new byte[24];
            CSRF_RANDOM.nextBytes(bytes);
            session.setAttribute(CSRF_SESSION_KEY,
                    Base64.getUrlEncoder().withoutPadding().encodeToString(bytes));
        }
    }


    @Override
    protected void doGet(
            HttpServletRequest request,
            HttpServletResponse response
    ) throws ServletException, IOException {

        response.setHeader("Cache-Control", "no-store");
        prepareFormToken(request);

        request.getRequestDispatcher(
                "/WEB-INF/views/forgot.jsp"
        ).forward(request, response);
    }

    @Override
    protected void doPost(
            HttpServletRequest request,
            HttpServletResponse response
    ) throws IOException {

        request.setCharacterEncoding("UTF-8");
        HttpSession csrfSession = request.getSession(false);
        Object expected = csrfSession == null ? null : csrfSession.getAttribute(CSRF_SESSION_KEY);
        String supplied = request.getParameter("csrfToken");
        if (!(expected instanceof String known) || supplied == null ||
                !MessageDigest.isEqual(known.getBytes(StandardCharsets.UTF_8),
                        supplied.getBytes(StandardCharsets.UTF_8))) {
            response.sendError(403, "Invalid reset request form token.");
            return;
        }
        csrfSession.removeAttribute(CSRF_SESSION_KEY);


        String email = request.getParameter("email");

        // Always return the same message, whether the
        // email exists or not, to avoid user enumeration.
        if (email == null || email.length() > 254) {
            redirectToConfirmation(request, response);
            return;
        }

        email = email.trim().toLowerCase(Locale.ROOT);

        if (email.isEmpty()) {
            redirectToConfirmation(request, response);
            return;
        }

        try {
            String apiKey = System.getenv("SENDGRID_API_KEY");
            String senderAddress = System.getenv("EMAIL_FROM");
            String baseUrl = System.getenv("APP_BASE_URL");

            if (apiKey == null || apiKey.isBlank()
                    || senderAddress == null || senderAddress.isBlank()
                    || baseUrl == null || baseUrl.isBlank()) {
                throw new IllegalStateException(
                        "SendGrid environment variables are missing."
                );
            }

            Instant now = Instant.now();

            String token =
                    PasswordResetTokenUtil.generateToken();

            String tokenHash =
                    PasswordResetTokenUtil.hashToken(token);

            Date expiresAt = Date.from(
                    now.plus(15, ChronoUnit.MINUTES)
            );

            // Allow at most one request per account per minute.
            // This is only a basic account-level cooldown.
            Date cooldown = Date.from(
                    now.minus(1, ChronoUnit.MINUTES)
            );

            Document user = UserCollection.getUsers()
                    .findOneAndUpdate(
                            Filters.and(
                                    Filters.eq("email", email),
                                    Filters.or(
                                            Filters.exists(
                                                    "resetRequestedAt", false
                                            ),
                                            Filters.lte(
                                                    "resetRequestedAt", cooldown
                                            )
                                    )
                            ),
                            Updates.combine(
                                    Updates.set(
                                            "resetTokenHash", tokenHash
                                    ),
                                    Updates.set(
                                            "resetExpiresAt", expiresAt
                                    ),
                                    Updates.set(
                                            "resetRequestedAt", Date.from(now)
                                    )
                            ),
                            new FindOneAndUpdateOptions()
                                    .returnDocument(ReturnDocument.AFTER)
                    );
getServletContext().log(
        "Password reset: MongoDB user matched = "
        + (user != null)
);
            if (user != null) {

                String resetLink =
                        baseUrl.replaceAll("/+$", "")
                        + "/reset?token=" + token;

                Email from = new Email(senderAddress);
                Email to = new Email(user.getString("email"));

                Content content = new Content(
                        "text/plain",
                        "You requested a password reset for NewsWeb.\n\n"
                        + "Open this link to reset your password:\n"
                        + resetLink
                        + "\n\nThe link expires in 15 minutes."
                        + "\nIf you did not request this, ignore this email."
                );

                Mail mail = new Mail(
                        from,
                        "Reset your NewsWeb password",
                        to,
                        content
                );

                SendGrid sendGrid = new SendGrid(apiKey);

                Request sendRequest = new Request();
                sendRequest.setMethod(Method.POST);
                sendRequest.setEndpoint("mail/send");
                sendRequest.setBody(mail.build());

                Response sendResponse =
                        sendGrid.api(sendRequest);
getServletContext().log(
        "SendGrid HTTP status: "
        + sendResponse.getStatusCode()
);
                if (sendResponse.getStatusCode() != 202) {
                    throw new IOException(
                            "SendGrid rejected email. Status: "
                            + sendResponse.getStatusCode()
                    );
		
                }
            }

        } catch (Exception e) {
            getServletContext().log(
                    "Forgot-password request failed.", e
            );
        }

        redirectToConfirmation(request, response);
    }

    private void redirectToConfirmation(
            HttpServletRequest request,
            HttpServletResponse response
    ) throws IOException {

        response.sendRedirect(
                request.getContextPath() + "/forgot?sent=1"
        );
    }
}
