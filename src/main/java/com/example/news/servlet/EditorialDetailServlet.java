
package com.example.news.servlet;

import com.example.news.util.EditorialCollection;
import com.example.news.util.RichTextUtil;

import com.mongodb.client.model.Filters;

import org.bson.Document;
import org.bson.types.ObjectId;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;

@WebServlet("/editorial")
public class EditorialDetailServlet extends HttpServlet {

    @Override
    protected void doGet(
            HttpServletRequest request,
            HttpServletResponse response
    ) throws ServletException, IOException {

        try {

            // Get the article ID from the URL.
            String id = request.getParameter("id");

            // Reject missing or invalid MongoDB ObjectIds.
            if (id == null || !ObjectId.isValid(id)) {
                response.sendError(
                        HttpServletResponse.SC_BAD_REQUEST,
                        "Invalid article ID."
                );
                return;
            }

            ObjectId articleId = new ObjectId(id);

            // Public readers can only view published articles.
            Document article =
                    EditorialCollection.getCollection()
                            .find(Filters.and(
                                    Filters.eq("_id", articleId),
                                    Filters.eq("status", "published")
                            ))
                            .first();

            if (article == null) {
                response.sendError(
                        HttpServletResponse.SC_NOT_FOUND,
                        "Article not found."
                );
                return;
            }

            
 // Prepare sanitized HTML for public display.
String storedHtml = article.getString("contentHtml");

if (storedHtml != null && !storedHtml.isBlank()) {

    String safeHtml =
            RichTextUtil.sanitize(storedHtml);

    article.put("safeContentHtml", safeHtml);
}

// Pass the article to the JSP.
request.setAttribute("article", article);


            request.getRequestDispatcher(
                    "/WEB-INF/views/editorial-detail.jsp"
            ).forward(request, response);

        } catch (Exception e) {

            getServletContext().log(
                    "Unable to load editorial article.", e
            );

            response.sendError(
                    HttpServletResponse.SC_INTERNAL_SERVER_ERROR,
                    "Unable to load article."
            );
        }
    }
}
