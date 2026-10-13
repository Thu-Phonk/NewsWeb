
<%@ page contentType="text/html; charset=UTF-8"
         pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>

<!DOCTYPE html>
<html lang="en">
<head>
    <meta charset="UTF-8">
    <meta name="viewport"
          content="width=device-width, initial-scale=1">

    <title>Blog Moderation - NewsWeb</title>

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

        header a { color: white; }

        main {
            max-width: 950px;
            margin: 30px auto;
        }

        .post {
            background: white;
            border-radius: 8px;
            padding: 25px;
            margin-bottom: 20px;
        }

        .meta {
            font-size: 14px;
            color: #666;
        }

        .content {
            white-space: pre-wrap;
            overflow-wrap: anywhere;
            line-height: 1.7;
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
            margin-top: 10px;
        }

        .approve { background: #237a39; }
        .reject { background: #ba3030; }
        .success { color: green; }
    </style>
</head>

<body>

<header>
    <h1>Community Blog Moderation</h1>
    <p>Review posts submitted by other users</p>

    <a href="${pageContext.request.contextPath}/editor">
        Editorial News Reviews
    </a>
</header>

<main>

    <c:if test="${param.reviewed eq 'approved'}">
        <p class="success">Blog approved successfully!</p>
    </c:if>

    <c:if test="${param.reviewed eq 'rejected'}">
        <p class="success">Blog rejected successfully.</p>
    </c:if>

    <c:choose>

        <c:when test="${empty pendingBlogs}">
            <div class="post">
                <h2>No blogs available for review</h2>

                <p>
                    There are no pending blogs from other
                    users in your review queue.
                </p>
            </div>
        </c:when>

        <c:otherwise>

            <c:forEach var="blog" items="${pendingBlogs}">

                <article class="post">

                    <h2><c:out value="${blog.title}" /></h2>

                    <p class="meta">
                        Category:
                        <c:out value="${blog.category}" />
                    </p>

                    <p class="meta">
                        Author:
                        <c:out value="${blog.authorName}" />
                    </p>

                    <div class="content"><c:out value="${blog.content}" /></div>

                    <hr>

                    <form
                        action="${pageContext.request.contextPath}/editor/blogs"
                        method="post">

                        <input type="hidden"
                               name="id"
                               value="<c:out value='${blog._id}' />">

                        <input type="hidden"
                               name="csrfToken"
                               value="<c:out value='${sessionScope.blogReviewCsrfToken}' />">

                        <label for="reason-${blog._id}">
                            Rejection Reason
                        </label>

                        <textarea id="reason-${blog._id}"
                                  name="rejectionReason"
                                  rows="3"
                                  maxlength="500"
                                  placeholder="Required when rejecting"></textarea>

                        <div>
                            <button type="submit"
                                    class="approve"
                                    name="action"
                                    value="approve">
                                Approve Blog
                            </button>

                            <button type="submit"
                                    class="reject"
                                    name="action"
                                    value="reject">
                                Reject Blog
                            </button>
                        </div>

                    </form>

                </article>

            </c:forEach>

        </c:otherwise>

    </c:choose>

</main>
</body>
</html>
