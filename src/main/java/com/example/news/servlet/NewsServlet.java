
package com.example.news.servlet;

import com.example.news.util.NewsCollection;
import com.example.news.util.EditorialCollection;
import com.example.news.util.RoleUtil;

import com.mongodb.client.MongoCollection;
import com.mongodb.client.model.Filters;
import com.mongodb.client.model.Sorts;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import org.bson.Document;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

@WebServlet("/news")
public class NewsServlet extends HttpServlet {

    @Override
    protected void doGet(
            HttpServletRequest request,
            HttpServletResponse response
    ) throws ServletException, IOException {

        try {

            // Load regular news.
            MongoCollection<Document> collection =
                    NewsCollection.getCollection();

            List<Document> newsList = new ArrayList<>();

            for (Document news : collection.find()
                    .sort(Sorts.descending("createdAt"))) {

                newsList.add(news);
            }

            // Load published editorial articles only.
            List<Document> editorialList = new ArrayList<>();

            for (Document article :
                    EditorialCollection.getCollection()
                            .find(Filters.eq("status", "published"))
                            .sort(Sorts.descending("reviewedAt"))
                            .limit(50)) {

                editorialList.add(article);
            }

            request.setAttribute("newsList", newsList);
            request.setAttribute("editorialList", editorialList);

            // Determine which dashboard links the user may see.
            Document currentUser = RoleUtil.getCurrentUser(request);

            boolean canWrite = currentUser != null
                    && RoleUtil.hasRole(
                            currentUser,
                            "writer", "editor", "administrator"
                    );

            boolean canEdit = currentUser != null
                    && RoleUtil.hasRole(
                            currentUser,
                            "editor", "administrator"
                    );

            boolean isAdmin = currentUser != null
                    && RoleUtil.hasRole(
                            currentUser,
                            "administrator"
                    );

            request.setAttribute("canWrite", canWrite);
            request.setAttribute("canEdit", canEdit);
            request.setAttribute("isAdmin", isAdmin);

            request.getRequestDispatcher("/news.jsp")
                    .forward(request, response);

        } catch (Exception e) {

            getServletContext().log(
                    "Unable to load news.", e
            );

            response.sendError(
                    500, "Unable to load news."
            );
        }
    }
}
