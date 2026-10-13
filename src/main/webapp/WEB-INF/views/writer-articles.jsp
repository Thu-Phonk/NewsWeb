
<%@ page contentType="text/html; charset=UTF-8"
         pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>

<!DOCTYPE html>
<html lang="en">
<head>
    <meta charset="UTF-8">
    <meta name="viewport"
          content="width=device-width, initial-scale=1">

    <title>My Editorial Articles - NewsWeb</title>

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
            margin: 0 10px;
        }

        main {
            max-width: 900px;
            margin: 30px auto;
        }

        .article {
            background: white;
            padding: 25px;
            margin-bottom: 20px;
            border-radius: 8px;
        }

        .meta {
            color: #666;
            font-size: 14px;
        }

        .content {
            line-height: 1.7;
            white-space: pre-wrap;
            overflow-wrap: anywhere;
        }

        .status {
            font-weight: bold;
        }

        .pending { color: #996300; }
        .published { color: #237a39; }
        .rejected { color: #ba3030; }

        .feedback {
            background: #fff1f1;
            border-left: 4px solid #ba3030;
            padding: 15px;
            margin-top: 16px;
        }

        .button {
            display: inline-block;
            padding: 10px 16px;
            background: #222;
            color: white;
            text-decoration: none;
            border-radius: 5px;
            margin: 15px 0;
        }
    </style>
</head>

<body>

<header>
    <h1>My Editorial Articles</h1>

    <p>
        Welcome,
        <c:out value="${writerName}" />
    </p>

    <nav>
        <a href="${pageContext.request.contextPath}/writer">
            Writer Dashboard
        </a>

        <a href="${pageContext.request.contextPath}/news">
            News
        </a>

        <a href="${pageContext.request.contextPath}/blogs">
            Community Blogs
        </a>
    </nav>
</header>

<main>

    <a class="button"
       href="${pageContext.request.contextPath}/writer/submit">
        + Submit New Article
    </a>

    <c:choose>

        <c:when test="${empty articles}">

            <div class="article">
                <h2>No editorial articles yet</h2>

                <p>
                    You haven't submitted any editorial
                    articles for review.
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
                        Category:
                        <c:out value="${article.category}" />
                    </p>

                    <p>
                        Status:

                        <c:choose>
                            <c:when test="${article.status eq 'pending'}">
                                <span class="status pending">
                                    Pending Review
                                </span>
                            </c:when>

                            <c:when test="${article.status eq 'published'}">
                                <span class="status published">
                                    Published
                                </span>
                            </c:when>

                            <c:when test="${article.status eq 'rejected'}">
                                <span class="status rejected">
                                    Rejected
                                </span>

<c:url var="reviseUrl" value="/writer/revise">
    <c:param name="id" value="${article._id}" />
</c:url>

<p>
    <a class="button" href="${reviseUrl}">
        Revise and Resubmit
    </a>
</p>

                            </c:when>

                            <c:otherwise>
                                <c:out value="${article.status}" />
                            </c:otherwise>
                        </c:choose>
                    </p>

                    <h3>Article Content</h3>

                    <div class="content"><c:out value="${article.content}" /></div>

                    <c:if test="${article.status eq 'pending'}">
                        <p class="meta">
                            Your article is awaiting editor review.
                        </p>
                    </c:if>

                    <c:if test="${article.status eq 'published'}">
                        <p class="meta">
                            Your article has been approved.
                            It is now visible on the News page.
                        </p>
                    </c:if>

                    <c:if test="${article.status eq 'rejected'}">
                        <div class="feedback">
                            <strong>Editor's Feedback</strong>

                            <p>
                                <c:out
                                    value="${article.rejectionReason}"
                                    default="No feedback provided." />
                            </p>
                        </div>
                    </c:if>

                </article>

            </c:forEach>

        </c:otherwise>

    </c:choose>

</main>

</body>
</html>
