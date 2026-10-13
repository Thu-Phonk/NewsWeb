
package com.example.news.servlet;

import com.example.news.util.EditorialCollection;
import com.example.news.util.RoleUtil;

import com.mongodb.client.model.Filters;
import com.mongodb.client.model.Sorts;

import org.bson.Document;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

@WebServlet("/writer/articles")
public class WriterArticlesServlet extends HttpServlet {

    @Override
    protected void doGet(
            HttpServletRequest request,
            HttpServletResponse response
    ) throws ServletException, IOException {

        response.setHeader("Cache-Control", "no-store");

        try {
            // Get the currently logged-in user
            // directly from MongoDB.
            Document user =
                    RoleUtil.getCurrentUser(request);

            if (user == null) {
                response.sendRedirect(
                        request.getContextPath() + "/login"
                );
                return;
            }

            // Only editorial roles may use this page.
            if (!RoleUtil.hasRole(
                    user,
                    "writer",
                    "editor",
                    "administrator")) {

                response.sendError(
                        HttpServletResponse.SC_FORBIDDEN,
                        "Writer permission required."
                );
                return;
            }

            List<Document> articles = new ArrayList<>();

            // The author ID comes from the authenticated user,
            // never from a browser query parameter.
            for (Document article :
                    EditorialCollection.getCollection()
                        .find(Filters.eq(
                                "authorId",
                                user.getObjectId("_id")
                        ))
                        .sort(Sorts.descending("createdAt"))
                        .limit(100)) {

                articles.add(article);
            }

            request.setAttribute("articles", articles);
            request.setAttribute(
                    "writerName",
                    user.getString("username")
            );

            request.getRequestDispatcher(
                    "/WEB-INF/views/writer-articles.jsp"
            ).forward(request, response);

        } catch (Exception e) {
            getServletContext().log(
                    "Unable to load writer articles.", e
            );

            response.sendError(
                    HttpServletResponse.SC_INTERNAL_SERVER_ERROR,
                    "Unable to load your articles."
            );
        }
    }
}
