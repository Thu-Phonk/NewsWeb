
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
            line-height: 1.7;
            white-space: pre-wrap;
            overflow-wrap: anywhere;
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

    <a href="${pageContext.request.contextPath}/news"
       style="color:white;">
        Back to News
    </a>
</header>
<p>
    <a href="${pageContext.request.contextPath}/editor/blogs"
       style="color:white;">
        Review Community Blogs
    </a>
</p>
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

                    <div class="content"><c:out value="${article.content}" /></div>

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
