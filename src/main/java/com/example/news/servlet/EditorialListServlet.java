
package com.example.news.servlet;

import com.example.news.util.EditorialCollection;

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

@WebServlet("/editorials")
public class EditorialListServlet extends HttpServlet {

    @Override
    protected void doGet(
            HttpServletRequest request,
            HttpServletResponse response
    ) throws ServletException, IOException {

        try {
            List<Document> articles = new ArrayList<>();

            // Public visitors can only see approved articles.
            for (Document article :
                    EditorialCollection.getCollection()
                            .find(Filters.eq("status", "published"))
                            .sort(Sorts.descending("createdAt"))
                            .limit(100)) {

                // Prepare a short article preview.
                String content = article.getString("content");

                if (content == null) {
                    content = "";
                }

                String excerpt = content.length() > 180
                        ? content.substring(0, 180) + "..."
                        : content;

                article.append("excerpt", excerpt);
                articles.add(article);
            }

            request.setAttribute("articles", articles);

            request.getRequestDispatcher(
                    "/WEB-INF/views/editorials.jsp"
            ).forward(request, response);

        } catch (Exception e) {
            getServletContext().log(
                    "Unable to load public editorials.", e
            );

            response.sendError(
                    500, "Unable to load editorial articles."
            );
        }
    }
}
