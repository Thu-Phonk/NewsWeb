package com.example.news.servlet;

import com.example.news.util.EditorialCollection;
import com.mongodb.client.model.Filters;
import com.mongodb.client.model.Sorts;
import org.bson.Document;
import org.bson.conversions.Bson;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Collections;
import java.util.regex.Pattern;

@WebServlet("/editorials")
public class EditorialListServlet extends HttpServlet {
    private static final int PAGE_SIZE = 10;
    // Prevent an extremely large skip value from a manipulated URL.
    private static final int MAX_PAGES = 10000;

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        request.setCharacterEncoding("UTF-8");
        response.setContentType("text/html;charset=UTF-8");

        try {
            String query = clean(request.getParameter("q"), 100);
            String category = clean(request.getParameter("category"), 100);
            String sort = "oldest".equals(request.getParameter("sort"))
                    ? "oldest" : "newest";

            // Only categories of published posts appear in the filter.
            List<String> categories = new ArrayList<>();
            for (String candidate : EditorialCollection.getCollection()
                    .distinct("category", Filters.eq("status", "published"), String.class)) {
                if (candidate != null && !candidate.isBlank()) {
                    categories.add(candidate);
                }
            }
            Collections.sort(categories, String.CASE_INSENSITIVE_ORDER);

            List<Bson> terms = new ArrayList<>();
            terms.add(Filters.eq("status", "published"));
            if (!query.isEmpty()) {
                // Quote the user string: search text is not a regex program.
                terms.add(Filters.regex("title",
                        Pattern.compile(Pattern.quote(query), Pattern.CASE_INSENSITIVE)));
            }
            if (!category.isEmpty()) {
                terms.add(Filters.eq("category", category));
            }
            Bson filter = Filters.and(terms);

            long totalMatches = EditorialCollection.getCollection().countDocuments(filter);
            int totalPages = (int) Math.max(1L, Math.min((long) MAX_PAGES,
                    (totalMatches + PAGE_SIZE - 1) / PAGE_SIZE));
            int page = parsePage(request.getParameter("page"));
            page = Math.min(page, totalPages);

            Bson ordering = "oldest".equals(sort)
                    ? Sorts.ascending("createdAt", "_id")
                    : Sorts.descending("createdAt", "_id");

            List<Document> items = new ArrayList<>();
            for (Document item : EditorialCollection.getCollection()
                    .find(filter)
                    .sort(ordering)
                    .skip((page - 1) * PAGE_SIZE)
                    .limit(PAGE_SIZE)) {
                String content = item.getString("content");
                if (content == null) content = "";
                item.put("excerpt", content.length() > 180
                        ? content.substring(0, 180) + "..." : content);
                items.add(item);
            }

            request.setAttribute("articles", items);
            request.setAttribute("categories", categories);
            request.setAttribute("query", query);
            request.setAttribute("selectedCategory", category);
            request.setAttribute("sortOrder", sort);
            request.setAttribute("currentPage", page);
            request.setAttribute("totalPages", totalPages);
            request.setAttribute("totalMatches", totalMatches);
            request.setAttribute("hasPrevious", page > 1);
            request.setAttribute("hasNext", page < totalPages);
            request.getRequestDispatcher("/WEB-INF/views/editorials.jsp")
                    .forward(request, response);
        } catch (Exception e) {
            getServletContext().log("Unable to load /editorials listing.", e);
            if (!response.isCommitted()) {
                response.sendError(500, "Unable to load published posts.");
            }
        }
    }

    private static String clean(String value, int maxLength) {
        if (value == null) return "";
        String trimmed = value.trim();
        return trimmed.length() > maxLength
                ? trimmed.substring(0, maxLength) : trimmed;
    }

    private static int parsePage(String raw) {
        if (raw == null) return 1;
        try {
            return Math.max(1, Integer.parseInt(raw));
        } catch (NumberFormatException ex) {
            return 1;
        }
    }
}
