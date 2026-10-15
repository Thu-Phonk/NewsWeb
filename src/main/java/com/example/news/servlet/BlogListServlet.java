
package com.example.news.servlet;

import com.example.news.util.BlogCollection;
import com.mongodb.client.model.Filters;
import com.mongodb.client.model.Sorts;
import com.example.news.util.RichTextUtil;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import org.bson.Document;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

@WebServlet("/blogs")
public class BlogListServlet extends HttpServlet {

    @Override
    protected void doGet(
            HttpServletRequest request,
            HttpServletResponse response
    ) throws ServletException, IOException {

        response.setContentType("text/html;charset=UTF-8");

        try {
            List<Document> blogList = new ArrayList<>();

            for (Document blog : BlogCollection.getCollection()
                    .find(Filters.eq("status", "published"))
                    .sort(Sorts.descending("createdAt"))
                    .limit(50)) {

                blogList.add(blog);
            }

for (Document blog : blogList) {

    String storedHtml = blog.getString("contentHtml");

    if (storedHtml != null && !storedHtml.isBlank()) {

        String safeHtml =
                RichTextUtil.sanitize(storedHtml);

        blog.put("safeContentHtml", safeHtml);
    }
}

            request.setAttribute("blogList", blogList);

            request.getRequestDispatcher(
                    "/WEB-INF/views/blogs.jsp"
            ).forward(request, response);

        } catch (Exception e) {
            getServletContext().log(
                    "Unable to load blogs.", e
            );
            response.sendError(
                    HttpServletResponse.SC_INTERNAL_SERVER_ERROR,
                    "Unable to load blogs."
            );
        }
    }
}
