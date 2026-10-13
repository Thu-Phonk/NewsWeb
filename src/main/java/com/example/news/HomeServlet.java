package com.example.news;

import java.io.IOException;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

@WebServlet("/")
public class HomeServlet extends HttpServlet {

    @Override
    protected void doGet(
            HttpServletRequest request,
            HttpServletResponse response)
            throws ServletException, IOException {

        response.setContentType("text/html;charset=UTF-8");

        response.getWriter().println("""
            <!DOCTYPE html>
            <html>
            <head>
                <meta charset="UTF-8">
                <title>News Website</title>
            </head>
            <body>

                <h1>News Website</h1>

                <p>Java Servlet backend is working!</p>

            </body>
            </html>
            """);
    }
}