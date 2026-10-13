package com.example.news.servlet;

import com.example.news.util.MongoDBUtil;
import com.mongodb.client.MongoDatabase;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;

@WebServlet("/database-test")
public class DatabaseTestServlet extends HttpServlet {

    @Override
    protected void doGet(HttpServletRequest request,
                         HttpServletResponse response)
            throws ServletException, IOException {

        response.setContentType("text/html;charset=UTF-8");

        try {
            MongoDatabase database = MongoDBUtil.getDatabase();

            // Forces an actual request to MongoDB
            database.listCollectionNames().first();

            response.getWriter().println("""
                <!DOCTYPE html>
                <html>
                <head>
                    <title>Database Test</title>
                </head>
                <body>
                    <h1>MongoDB Connection Successful!</h1>
                    <p>Database: news_project</p>
                </body>
                </html>
                """);

        } catch (Exception e) {

            response.setStatus(
                    HttpServletResponse.SC_INTERNAL_SERVER_ERROR
            );

            response.getWriter().println("""
                <!DOCTYPE html>
                <html>
                <head>
                    <title>Database Error</title>
                </head>
                <body>
                    <h1>MongoDB Connection Failed</h1>
                    <pre>
                """ + e.getMessage() + """
                    </pre>
                </body>
                </html>
                """);
        }
    }
}