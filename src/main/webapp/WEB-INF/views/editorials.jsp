
<%@ page contentType="text/html; charset=UTF-8"
         pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>

<!DOCTYPE html>
<html lang="en">

<head>
    <meta charset="UTF-8">
    <meta name="viewport"
          content="width=device-width, initial-scale=1">

    <title>Editorial Articles - NewsWeb</title>

    <style>
        body {
            font-family: Arial, sans-serif;
            background: #f4f4f4;
            margin: 0;
        }

        header {
            background: #222;
            color: white;
            text-align: center;
            padding: 25px;
        }

        header a {
            color: white;
            margin: 0 12px;
            text-decoration: none;
        }

        main {
            max-width: 1000px;
            margin: 30px auto;
        }

        .article {
            background: white;
            padding: 25px;
            margin-bottom: 20px;
            border-radius: 8px;
        }

        .article h2 {
            margin-top: 0;
        }

        .meta {
            color: #666;
            font-size: 14px;
        }

        .excerpt {
            line-height: 1.7;
            white-space: pre-wrap;
            overflow-wrap: anywhere;
        }

        .button {
            display: inline-block;
            padding: 10px 16px;
            background: #222;
            color: white;
            text-decoration: none;
            border-radius: 5px;
        }
    </style>
</head>

<body>

<header>
    <h1>NewsWeb Editorials</h1>
    <p>Approved articles from our writers</p>

    
<nav>
    <a href="${pageContext.request.contextPath}/">
        Home
    </a>

    <a href="${pageContext.request.contextPath}/news">
        News
    </a>

    <a href="${pageContext.request.contextPath}/editorials">
        Editorials
    </a>

    <a href="${pageContext.request.contextPath}/blogs">
        Community Blogs
    </a>
</nav>

</header>

<main>

    <h1>Editorial Articles</h1>

    <c:choose>

        <c:when test="${empty articles}">

            <div class="article">
                <h2>No published articles yet</h2>

                <p>
                    There are no approved editorial
                    articles available right now.
                </p>
            </div>

        </c:when>

        <c:otherwise>

            <c:forEach var="article" items="${articles}">

                <article class="article">

                    <h2>
                        <c:out value="${article.title}" />
                    </h2>

                    <p class="meta">
                        By <c:out value="${article.authorName}" />
                        |
                        <c:out value="${article.category}" />
                    </p>

                    <p class="excerpt"><c:out value="${article.excerpt}" /></p>

                    <c:url var="readUrl" value="/editorial">
                        <c:param name="id"
                                 value="${article._id}" />
                    </c:url>

                    <a class="button" href="${readUrl}">
                        Read Full Article
                    </a>

                </article>

            </c:forEach>

        </c:otherwise>

    </c:choose>

</main>

</body>
</html>
