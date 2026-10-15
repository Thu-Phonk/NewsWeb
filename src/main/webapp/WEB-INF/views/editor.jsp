
<%@ page contentType="text/html; charset=UTF-8"
         pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>

<!DOCTYPE html>
<html lang="en">
<head>
    <meta charset="UTF-8">
    <meta name="viewport"
          content="width=device-width, initial-scale=1">

    <title>Editor Dashboard - NewsWeb</title>

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

        main {
            max-width: 950px;
            margin: 30px auto;
        }

        .article {
            background: white;
            padding: 25px;
            border-radius: 8px;
            margin-bottom: 20px;
        }

        .meta {
            color: #666;
            font-size: 14px;
        }

        
.content {
    line-height: 1.8;
    overflow-wrap: anywhere;
}

.content p {
    margin: 0 0 14px;
}

.content h2 {
    font-size: 24px;
    margin: 22px 0 12px;
}

.content h3 {
    font-size: 20px;
    margin: 18px 0 10px;
}

.content ul {
    list-style-type: disc;
    padding-left: 28px;
}

.content ol {
    list-style-type: decimal;
    padding-left: 28px;
}

.content blockquote {
    border-left: 4px solid #999;
    background: #f7f7f7;
    padding: 10px 18px;
    margin: 18px 0;
    font-style: italic;
}

.content a {
    color: #1565c0;
    text-decoration: underline;
}

.plain-content {
    white-space: pre-wrap;
}

        textarea {
            box-sizing: border-box;
            width: 100%;
            padding: 10px;
            margin-top: 10px;
        }

        button {
            padding: 10px 20px;
            border: none;
            border-radius: 5px;
            color: white;
            cursor: pointer;
            margin-top: 12px;
            margin-right: 8px;
        }

        .approve { background: #237a39; }
        .reject { background: #ba3030; }
        .success { color: green; }
    </style>
</head>

<body>


<header>
    <h1>Editor Dashboard</h1>
    <p>Review Submitted Articles</p>

    <nav>
        <a href="${pageContext.request.contextPath}/news"
           style="color:white; margin-right:20px;">
            Back to News
        </a>

        <a href="${pageContext.request.contextPath}/editor/blogs"
           style="color:white;">
            Review Community Blogs
        </a>
    </nav>
</header>

<main>

<main>

    <c:if test="${param.reviewed eq 'approved'}">
        <p class="success">
            Article approved successfully!
        </p>
    </c:if>

    <c:if test="${param.reviewed eq 'rejected'}">
        <p class="success">
            Article rejected successfully.
        </p>
    </c:if>

    <c:choose>

        <c:when test="${empty pendingArticles}">
            <div class="article">
                <h2>No pending articles</h2>
                <p>There are no submissions awaiting review.</p>
            </div>
        </c:when>

        <c:otherwise>

            <c:forEach var="article"
                       items="${pendingArticles}">

                <div class="article">

                    <h2>
                        <c:out value="${article.title}" />
                    </h2>

                    <p class="meta">
                        Category:
                        <c:out value="${article.category}" />
                    </p>

                    <p class="meta">
                        Submitted by:
                        <c:out value="${article.authorName}" />
                    </p>

                    <p class="meta">
                        Status:
                        <c:out value="${article.status}" />
                    </p>

                    
<div class="content">

    <c:choose>

        <%-- New articles with sanitized rich-text content --%>
        <c:when test="${not empty article.safeContentHtml}">
            <c:out value="${article.safeContentHtml}"
                   escapeXml="false" />
        </c:when>

        <%-- Older articles stored as plain text --%>
        <c:otherwise>
            <div class="plain-content">
                <c:out value="${article.content}" />
            </div>
        </c:otherwise>

    </c:choose>

</div>


                    <hr>

                    <form action="${pageContext.request.contextPath}/editor"
                          method="post">

                        <input type="hidden"
                               name="id"
                               value="<c:out value='${article._id}' />">

                        <input type="hidden"
                               name="csrfToken"
                               value="<c:out value='${sessionScope.editorCsrfToken}' />">

                        <label for="reason-${article._id}">
                            Rejection Reason
                            (required only when rejecting)
                        </label>

                        <textarea
                            id="reason-${article._id}"
                            name="rejectionReason"
                            rows="3"
                            maxlength="500"
                            placeholder="Explain why the article was rejected"></textarea>

                        <div>
                            <button class="approve"
                                    type="submit"
                                    name="action"
                                    value="approve">
                                Approve Article
                            </button>

                            <button class="reject"
                                    type="submit"
                                    name="action"
                                    value="reject">
                                Reject Article
                            </button>
                        </div>

                    </form>
                </div>

            </c:forEach>

        </c:otherwise>

    </c:choose>

</main>
</body>
</html>
