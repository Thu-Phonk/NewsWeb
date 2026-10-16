package com.example.news.servlet;

import com.example.news.util.ReactionCollection;
import com.example.news.util.ReactionUtil;
import com.example.news.util.RoleUtil;
import com.mongodb.client.model.Filters;
import com.mongodb.client.model.Sorts;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.bson.Document;
import org.bson.types.ObjectId;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

@WebServlet("/my-bookmarks")
public class MyBookmarksServlet extends HttpServlet {
    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        response.setHeader("Cache-Control", "no-store");
        try {
            Document user = RoleUtil.getCurrentUser(request);
            if (user == null) {
                response.sendRedirect(request.getContextPath() + "/login"); return;
            }
            List<Document> bookmarks = new ArrayList<>();
            for (Document reaction : ReactionCollection.getCollection()
                .find(Filters.and(Filters.eq("userId", user.getObjectId("_id")),
                                  Filters.eq("kind", "bookmark")))
                .sort(Sorts.descending("createdAt", "_id"))
                .limit(50)) {
                String type = reaction.getString("targetType");
                ObjectId id = reaction.getObjectId("targetId");
                Document target = ReactionUtil.publishedTarget(type, id);
                if (target == null) continue; // Don't reveal unpublished content.
                String url = "editorial".equals(type) ? "/editorial?id=" : "/blog/view?id=";
                bookmarks.add(new Document("title", target.getString("title"))
                    .append("category", target.getString("category"))
                    .append("coverImageUrl", target.getString("coverImageUrl"))
                    .append("targetType", type)
                    .append("url", request.getContextPath() + url + id.toHexString()));
            }
            request.setAttribute("bookmarks", bookmarks);
            request.getRequestDispatcher("/WEB-INF/views/my-bookmarks.jsp")
                    .forward(request, response);
        } catch (Exception e) {
            getServletContext().log("Unable to load bookmarks.", e);
            if (!response.isCommitted()) response.sendError(500, "Unable to load bookmarks.");
        }
    }
}
