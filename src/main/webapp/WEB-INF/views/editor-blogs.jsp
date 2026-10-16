
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

        
.review-cover {
    display: block;
    width: 100%;
    max-height: 380px;
    aspect-ratio: 16 / 9;
    object-fit: cover;
    border-radius: 8px;
    margin-bottom: 20px;
}

.content {
    overflow-wrap: anywhere;
    line-height: 1.8;
}

.content p {
    margin: 0 0 14px;
}

.content h2,
.content h3 {
    margin: 20px 0 12px;
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
    background: #f7f7f7;
    border-left: 4px solid #999;
    padding: 10px 18px;
    margin: 18px 0;
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

<%-- Cover image submitted by the blog author --%>
<c:if test="${not empty blog.coverImageUrl}">

    <img
        src="<c:out value='${blog.coverImageUrl}' />"
        alt="Blog cover image"
        class="review-cover"
        loading="lazy">

</c:if>

                    <h2><c:out value="${blog.title}" /></h2>

                    <p class="meta">
                        Category:
                        <c:out value="${blog.category}" />
                    </p>

                    <p class="meta">
                        Author:
                        <c:out value="${blog.authorName}" />
                    </p>

                    
<div class="content">

    <c:choose>

        <c:when test="${not empty blog.safeContentHtml}">
            <c:out value="${blog.safeContentHtml}"
                   escapeXml="false" />
        </c:when>

        <c:otherwise>
            <div class="plain-content">
                <c:out value="${blog.content}" />
            </div>
        </c:otherwise>

    </c:choose>

</div>


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
