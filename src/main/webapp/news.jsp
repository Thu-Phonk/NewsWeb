
<%@ page contentType="text/html; charset=UTF-8"
         pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>

<!DOCTYPE html>
<html lang="en">
<head>
    <meta charset="UTF-8">
    <meta name="viewport"
          content="width=device-width, initial-scale=1">

    <title>News Website</title>

    <style>
        body {
            font-family: Arial, sans-serif;
            margin: 0;
            background: #f4f4f4;
        }

        header {
            background: #222;
            color: white;
            padding: 20px;
            text-align: center;
        }

        header nav {
            margin-top: 20px;
        }

        header nav a {
            color: white;
            display: inline-block;
            margin: 8px;
        }

        main {
            width: 80%;
            max-width: 1000px;
            margin: 30px auto;
        }

        .news-card {
            background: white;
            padding: 20px;
            margin-bottom: 20px;
            border-radius: 8px;
        }

        .news-card h2 {
            margin-top: 0;
        }

        .category {
            color: #777;
            font-size: 14px;
        }

        .author {
            color: #555;
            font-size: 14px;
        }

        .content {
            line-height: 1.6;
            white-space: pre-wrap;
            overflow-wrap: anywhere;
        }

        .section-title {
            margin-top: 35px;
        }
    </style>
<link href="https://fonts.googleapis.com/css2?family=Inter:wght@400;500;600;700&family=Lora:wght@400;500;600;700&display=swap" rel="stylesheet">
<link rel="stylesheet" href="${pageContext.request.contextPath}/assets/gr6/navbar.css?v=1">
<link rel="stylesheet" href="${pageContext.request.contextPath}/assets/gr6/newsweb-theme.css?v=1">
</head>

<body>

<jsp:include page="/WEB-INF/views/partials/gr6-header.jsp" />

<main>

    <c:if test="${empty newsList and empty editorialList}">
        <h2>No news articles found.</h2>
    </c:if>

    <c:if test="${not empty newsList}">

        <h2 class="section-title">Latest News</h2>

        <c:forEach var="news" items="${newsList}">

            <article class="news-card">

                <h2>
                    <c:out value="${news.title}" />
                </h2>

                <p class="category">
                    Category:
                    <c:out value="${news.category}" />
                </p>

                <p class="author">
                    By:
                    <c:out value="${news.author}" />
                </p>

                <div class="content"><c:out value="${news.content}" /></div>

            </article>

        </c:forEach>

    </c:if>

    <c:if test="${not empty editorialList}">

        <h2 class="section-title">
            Published Editorial Articles
        </h2>

<p>
    <a href="${pageContext.request.contextPath}/editorials">
        Browse All Editorial Articles &rarr;
    </a>
</p>

        <c:forEach var="article"
                   items="${editorialList}">

            <article class="news-card">

                <h2>
                    <c:out value="${article.title}" />
                </h2>

                <p class="category">
                    Category:
                    <c:out value="${article.category}" />
                </p>

                <p class="author">
                    By:
                    <c:out value="${article.authorName}" />
                </p>

                <div class="content"><c:out value="${article.content}" /></div>

<c:url var="readUrl" value="/editorial">
    <c:param name="id" value="${article._id}" />
</c:url>

<p>
    <a href="${readUrl}">
        Read Full Editorial Article &rarr;
    </a>
</p>

            </article>

        </c:forEach>

    </c:if>

</main>

</body>
</html>
