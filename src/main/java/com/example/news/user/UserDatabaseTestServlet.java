
package com.example.news.servlet;

import com.example.news.user.UserCollection;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.model.Filters;

import org.bson.Document;

import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;

@WebServlet("/user-database-test")
public class UserDatabaseTestServlet extends HttpServlet {

    @Override
    protected void doGet(
            HttpServletRequest request,
            HttpServletResponse response
    ) throws IOException {

        response.setContentType("text/plain");
        response.setCharacterEncoding("UTF-8");

        try {
            MongoCollection<Document> users =
                    UserCollection.getUsers();

            long userCount = users.countDocuments();

            // Check whether the collection can be queried.
            // Do not print usernames, emails, or password hashes.
            boolean queryWorks = users.find(
                    Filters.exists("_id")
            ).limit(1).first() != null || userCount == 0;

            response.setStatus(HttpServletResponse.SC_OK);
            response.getWriter().println(
                    "User database connection successful!"
            );
            response.getWriter().println(
                    "Database: test"
            );
            response.getWriter().println(
                    "Collection: users"
            );
            response.getWriter().println(
                    "Total user documents: " + userCount
            );
            response.getWriter().println(
                    "Query test: " + (queryWorks ? "Passed" : "Failed")
            );

        } catch (Exception e) {
            getServletContext().log(
                    "User database test failed.", e
            );

            response.setStatus(
                    HttpServletResponse.SC_INTERNAL_SERVER_ERROR
            );
            response.getWriter().println(
                    "User database test failed. Check the Tomcat logs."
            );
        }
    }
}
