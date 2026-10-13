
package com.example.news.servlet;

import com.example.news.util.RoleUtil;

import org.bson.Document;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;

@WebServlet("/writer")
public class WriterServlet extends HttpServlet {

    @Override
    protected void doGet(
            HttpServletRequest request,
            HttpServletResponse response
    ) throws ServletException, IOException {

        response.setHeader("Cache-Control", "no-store");

        try {
            Document user = RoleUtil.getCurrentUser(request);

            if (user == null) {
                response.sendRedirect(
                        request.getContextPath() + "/login"
                );
                return;
            }

            if (!RoleUtil.hasRole(
                    user, "writer", "editor", "administrator")) {

                response.sendError(
                        HttpServletResponse.SC_FORBIDDEN,
                        "You do not have permission to access the Writer Dashboard."
                );
                return;
            }

            request.setAttribute(
                    "writerName", user.getString("username")
            );

            request.getRequestDispatcher(
                    "/WEB-INF/views/writer.jsp"
            ).forward(request, response);

        } catch (Exception e) {
            getServletContext().log(
                    "Writer authorization failed.", e
            );

            response.sendError(
                    500, "Unable to load Writer Dashboard."
            );
        }
    }
}
