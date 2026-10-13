
<%@ page contentType="text/html; charset=UTF-8"
         pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>

<!DOCTYPE html>
<html lang="en">
<head>
    <meta charset="UTF-8">
    <meta name="viewport"
          content="width=device-width, initial-scale=1">

    <title>My Posts - NewsWeb</title>

    <style>
        body {
            font-family: Arial, sans-serif;
            background: #f4f4f4;
            margin: 0;
        }

        header {
            background: #222;
            color: white;
            padding: 24px;
            text-align: center;
        }

        header a {
            color: white;
            margin: 0 10px;
        }

        main {
            max-width: 850px;
            margin: 30px auto;
        }

        .post {
            background: white;
            padding: 24px;
            margin-bottom: 20px;
            border-radius: 8px;
        }

        .post h2 {
            margin-top: 0;
        }

        .meta {
            color: #666;
            font-size: 14px;
        }

        .content {
            line-height: 1.6;
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
            margin-bottom: 20px;
        }
    </style>
</head>

<body>

<header>
    <h1>My Blog Posts</h1>

    <p>
        Welcome,
        <c:out value="${sessionScope.username}" />
    </p>

    <nav>
        <a href="${pageContext.request.contextPath}/news">
            News
        </a>

        <a href="${pageContext.request.contextPath}/blogs">
            Community Blogs
        </a>

        <a href="${pageContext.request.contextPath}/blog/my-posts">
            My Posts
        </a>
    </nav>
</header>

<main>
<c:if test="${param.updated eq '1'}">
    <p style="color:green;">
        Your changes were saved.
        The blog is now pending editor review.
    </p>
</c:if>
    <a class="button"
       href="${pageContext.request.contextPath}/blog/create">
        + Create New Post
    </a>

    <c:choose>

        <c:when test="${empty myBlogs}">
            <div class="post">
                <h2>No posts yet</h2>

                <p>
                    You haven't published any blog posts.
                </p>
            </div>
        </c:when>

        <c:otherwise>

            <c:forEach var="blog" items="${myBlogs}">

                <article class="post">

                    <h2>
                        <c:out value="${blog.title}" />
                    </h2>

                    <p class="meta">
                        Category:
                        <c:out value="${blog.category}" />
                    </p>

                    <p class="meta">
                        Status:
                        <c:out value="${blog.status}" />
                    </p>

                    <div class="content"><c:out value="${blog.content}" /></div>

<c:if test="${blog.status eq 'pending'}">
    <p style="color:#996300;">
        Awaiting editor approval.
    </p>
</c:if>

<c:if test="${blog.status eq 'rejected'}">
    <div style="background:#fff1f1;
                padding:15px;
                border-left:4px solid #ba3030;">

        <strong>Editor's Feedback:</strong>

        <p>
            <c:out value="${blog.rejectionReason}"
                   default="No feedback provided." />
        </p>

        <p>
            You can edit your blog and submit it
            for review again.
        </p>
    </div>
</c:if>

<p>
    <c:url var="editUrl" value="/blog/edit">
        <c:param name="id" value="${blog._id}" />
    </c:url>

    <a class="button" href="${editUrl}">
        Edit Post
    </a>
</p>

<form action="${pageContext.request.contextPath}/blog/delete"
      method="post"
      onsubmit="return confirm('Are you sure you want to delete this blog post? This cannot be undone.');">

    <input type="hidden"
           name="id"
           value="<c:out value='${blog._id}' />">

    <input type="hidden"
           name="csrfToken"
           value="<c:out value='${sessionScope.blogDeleteCsrfToken}' />">

    <button type="submit"
            style="background:#c62828;
                   color:white;
                   border:none;
                   padding:10px 16px;
                   border-radius:5px;
                   cursor:pointer;">
        Delete Post
    </button>
</form>

                </article>

            </c:forEach>

        </c:otherwise>

    </c:choose>

</main>

</body>
</html>
