
<%@ page contentType="text/html; charset=UTF-8"
         pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>

<!DOCTYPE html>
<html lang="en">

<head>
    <meta charset="UTF-8">
    <meta name="viewport"
          content="width=device-width, initial-scale=1">

    <title>Community Blogs - NewsWeb</title>

    <style>
        body {
            font-family: Arial, sans-serif;
            background: #f4f4f4;
            margin: 0;
        }

        header {
            background: #222;
            color: white;
            padding: 25px;
            text-align: center;
        }

        main {
            max-width: 850px;
            margin: 30px auto;
        }

        .blog-card {
            background: white;
            padding: 25px;
            margin-bottom: 20px;
            border-radius: 8px;
        }

        .author, .category {
            color: #666;
            font-size: 14px;
        }

        .content {
            line-height: 1.7;
            white-space: pre-wrap;
            overflow-wrap: anywhere;
        }

        nav a {
            color: white;
            margin: 0 10px;
        }

        .create-link {
            display: inline-block;
            margin-bottom: 20px;
        }
    </style>
</head>

<body>

<header>
    <h1>Community Blogs</h1>
    <p>Stories written by NewsWeb users</p>

    <nav>
        <a href="${pageContext.request.contextPath}/news">
            News
        </a>

        <a href="${pageContext.request.contextPath}/blogs">
            Blogs
        </a>

        <a href="${pageContext.request.contextPath}/login">
            Account
        </a>
<c:if test="${not empty sessionScope.userId}">
    <a href="${pageContext.request.contextPath}/blog/my-posts">
        My Posts
    </a>
</c:if>
    </nav>
</header>

<main>

    <c:if test="${not empty sessionScope.userId}">
        <a class="create-link"
           href="${pageContext.request.contextPath}/blog/create">
            + Write a Blog
        </a>
    </c:if>

    <c:choose>

        <c:when test="${empty blogList}">
            <h2>No blogs published yet.</h2>
        </c:when>

        <c:otherwise>

            <c:forEach var="blog" items="${blogList}">

                <article class="blog-card">

                    <h2>
                        <c:out value="${blog.title}" />
                    </h2>

                    <p class="category">
                        Category:
                        <c:out value="${blog.category}" />
                    </p>

                    <p class="author">
                        Written by:
                        <c:out value="${blog.authorName}" />
                    </p>

                    <div class="content"><c:out value="${blog.content}" /></div>

                </article>

            </c:forEach>

        </c:otherwise>

    </c:choose>

</main>

</body>
</html>
