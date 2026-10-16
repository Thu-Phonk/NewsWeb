package com.example.news.security;

import jakarta.servlet.Filter;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.ServletRequest;
import jakarta.servlet.ServletResponse;
import jakarta.servlet.annotation.WebFilter;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import java.io.IOException;
import java.security.SecureRandom;
import java.util.Base64;

@WebFilter("/*")
public class SecurityHeadersFilter implements Filter {
    private static final SecureRandom RANDOM = new SecureRandom();

    @Override
    public void doFilter(ServletRequest req, ServletResponse res, FilterChain chain)
            throws IOException, ServletException {
        HttpServletRequest request = (HttpServletRequest) req;
        HttpServletResponse response = (HttpServletResponse) res;
        response.setHeader("X-Content-Type-Options", "nosniff");
        response.setHeader("X-Frame-Options", "DENY");
        response.setHeader("Referrer-Policy", "no-referrer");
        response.setHeader("Permissions-Policy", "camera=(), microphone=(), geolocation=()");
        if (request.isSecure()) {
            response.setHeader("Strict-Transport-Security", "max-age=31536000");
        }
        String path = request.getServletPath();
        if (path.equals("/login") || path.equals("/register") || path.equals("/forgot")
                || path.equals("/reset") || path.startsWith("/admin/")
                || path.startsWith("/writer/") || path.equals("/writer")
                || path.startsWith("/editor/") || path.equals("/editor")
                || path.equals("/my-bookmarks")) {
            response.setHeader("Cache-Control", "no-store");
        }
        HttpSession session = request.getSession(false);
        if (session != null && session.getAttribute("userId") != null
                && session.getAttribute("logoutCsrfToken") == null) {
            byte[] bytes = new byte[24];
            RANDOM.nextBytes(bytes);
            session.setAttribute("logoutCsrfToken", Base64.getUrlEncoder().withoutPadding().encodeToString(bytes));
        }
        chain.doFilter(req, res);
    }
}
