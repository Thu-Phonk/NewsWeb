package com.example.news.servlet;

import com.example.news.util.NewsCollection;
import com.mongodb.client.MongoCollection;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.bson.Document;
import org.bson.types.ObjectId;

import java.io.IOException;
import java.time.LocalDateTime;

@WebServlet("/sample-news")
public class SampleNewsServlet extends HttpServlet {

    @Override
    protected void doGet(HttpServletRequest request,
                          HttpServletResponse response)
            throws ServletException, IOException {

        response.setContentType("text/html;charset=UTF-8");

        MongoCollection<Document> collection =
                NewsCollection.getCollection();

        Document news1 = new Document()
                .append("_id", new ObjectId())
                .append("title", "New AI Technology Released")
                .append("content",
                        "A new generation of artificial intelligence " +
                        "technology has been introduced, bringing new " +
                        "possibilities to developers and businesses.")
                .append("image", "ai.jpg")
                .append("author", "Admin")
                .append("category", "Technology")
                .append("createdAt", LocalDateTime.now().toString());

        Document news2 = new Document()
                .append("_id", new ObjectId())
                .append("title", "The Future of Web Development")
                .append("content",
                        "Modern web development continues to evolve " +
                        "with new tools, frameworks, and technologies.")
                .append("image", "web.jpg")
                .append("author", "Admin")
                .append("category", "Web Development")
                .append("createdAt", LocalDateTime.now().toString());

        Document news3 = new Document()
                .append("_id", new ObjectId())
                .append("title", "Java Servlets and Modern Web Applications")
                .append("content",
                        "Java Servlets remain an important foundation " +
                        "for understanding server-side web development.")
                .append("image", "java.jpg")
                .append("author", "Admin")
                .append("category", "Java")
                .append("createdAt", LocalDateTime.now().toString());

        collection.insertMany(
                java.util.List.of(news1, news2, news3)
        );

        response.getWriter().println("""
            <!DOCTYPE html>
            <html>
            <head>
                <meta charset="UTF-8">
                <title>Sample News</title>
            </head>
            <body>
                <h1>Sample News Inserted!</h1>
                <p>3 news articles were added to MongoDB.</p>
                <p>Collection: news</p>

                <a href="/NewsWeb/database-test">
                    Test MongoDB Connection
                </a>
            </body>
            </html>
            """);
    }
}