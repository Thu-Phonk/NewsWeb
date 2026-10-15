
<%@ page contentType="text/html; charset=UTF-8"
         pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>

<!DOCTYPE html>
<html lang="en">

<head>
    <meta charset="UTF-8">
    <meta name="viewport"
          content="width=device-width, initial-scale=1">

    <title>
        <c:out value="${article.title}" /> - NewsWeb
    </title>

    <style>
        body {
            font-family: Arial, sans-serif;
            background: #f4f4f4;
            margin: 0;
            color: #222;
        }

        header {
            background: #222;
            color: white;
            padding: 24px;
            text-align: center;
        }

        header h1 {
            margin: 0 0 12px;
        }

        header nav {
            margin-top: 18px;
        }

        header nav a {
            color: white;
            margin: 0 12px;
            text-decoration: none;
        }

        header nav a:hover {
            text-decoration: underline;
        }

        main {
            max-width: 850px;
            margin: 30px auto;
            padding: 0 16px;
        }

        .article {
            background: white;
            padding: 35px;
            border-radius: 10px;
            box-shadow: 0 2px 8px rgba(0,0,0,0.05);
        }

        .article-title {
            font-size: 34px;
            line-height: 1.25;
            margin-top: 0;
        }

        .category {
            color: #777;
            font-size: 14px;
            text-transform: uppercase;
        }

        .author {
            font-size: 14px;
            color: #555;
        }

        .divider {
            border: none;
            border-top: 1px solid #ddd;
            margin: 24px 0;
        }

        
.article-content {
    font-size: 17px;
    line-height: 1.9;
    overflow-wrap: anywhere;
}

.article-content p {
    margin: 0 0 16px;
}

.article-content h2 {
    font-size: 27px;
    margin: 28px 0 14px;
    line-height: 1.4;
}

.article-content h3 {
    font-size: 22px;
    margin: 24px 0 12px;
    line-height: 1.4;
}

.article-content ul {
    list-style-type: disc;
    padding-left: 30px;
    margin-bottom: 18px;
}

.article-content ol {
    list-style-type: decimal;
    padding-left: 30px;
    margin-bottom: 18px;
}

.article-content blockquote {
    border-left: 4px solid #777;
    background: #f7f7f7;
    margin: 20px 0;
    padding: 12px 20px;
    font-style: italic;
}

.article-content a {
    color: #1565c0;
    text-decoration: underline;
}

.plain-content {
    white-space: pre-wrap;
}


        .back-link {
            display: inline-block;
            margin-top: 30px;
            background: #222;
            color: white;
            padding: 12px 20px;
            border-radius: 5px;
            text-decoration: none;
        }

        @media (max-width: 600px) {
            .article {
                padding: 22px;
            }

            .article-title {
                font-size: 26px;
            }
        }
    </style>
</head>

<body>

<header>
    <h1>NewsWeb</h1>
    <p>Editorial Articles</p>

    <nav>
        <a href="${pageContext.request.contextPath}/news">
            Latest News
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

    <article class="article">

        <p class="category">
            <c:out value="${article.category}" />
        </p>

        <h1 class="article-title">
            <c:out value="${article.title}" />
        </h1>

        <p class="author">
            By
            <strong>
                <c:out value="${article.authorName}" />
            </strong>
        </p>

        <hr class="divider">

        
<div class="article-content">

    <c:choose>

        <%-- New articles: render sanitized formatted HTML --%>
        <c:when test="${not empty article.safeContentHtml}">
            <c:out value="${article.safeContentHtml}"
                   escapeXml="false" />
        </c:when>

        <%-- Older articles: preserve escaped plain text --%>
        <c:otherwise>
            <div class="plain-content">
                <c:out value="${article.content}" />
            </div>
        </c:otherwise>

    </c:choose>

</div>


        <a class="back-link"
           href="${pageContext.request.contextPath}/editorials">
            &larr; Back to Editorial Articles
        </a>

    </article>

</main>

</body>
</html>
