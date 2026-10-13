
<%@ page contentType="text/html; charset=UTF-8"
         pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>

<!DOCTYPE html>
<html lang="en">
<head>
    <meta charset="UTF-8">
    <meta name="viewport"
          content="width=device-width, initial-scale=1">

    <title>Create Blog - NewsWeb</title>

    <style>
        body {
            font-family: Arial, sans-serif;
            background: #f4f4f4;
            margin: 0;
        }

        main {
            max-width: 750px;
            margin: 40px auto;
            padding: 25px;
            background: white;
            border-radius: 8px;
        }

        label {
            display: block;
            margin-top: 18px;
            margin-bottom: 6px;
            font-weight: bold;
        }

        input, textarea {
            width: 100%;
            box-sizing: border-box;
            padding: 10px;
        }

        button {
            margin-top: 20px;
            padding: 12px 24px;
            cursor: pointer;
        }

        .success { color: green; }
        .error { color: red; }
    </style>
</head>

<body>
<main>

    <h1>Create a Blog Post</h1>

    <p>
        Writing as:
        <strong>
            <c:out value="${sessionScope.username}" />
        </strong>
    </p>

    <c:if test="${param.created eq '1'}">
    <p class="success">
        Your blog was submitted successfully!
        An editor must approve it before publication.
    </p>
</c:if>

    <c:if test="${not empty error}">
        <p class="error">
            <c:out value="${error}" />
        </p>
    </c:if>

    <form action="${pageContext.request.contextPath}/blog/create"
          method="post">

        <input type="hidden"
               name="csrfToken"
               value="<c:out value='${sessionScope.blogCsrfToken}' />">

        <label for="title">Blog Title</label>
        <input type="text"
               id="title"
               name="title"
               minlength="3"
               maxlength="150"
               required>

        <label for="category">Category</label>
        <input type="text"
               id="category"
               name="category"
               placeholder="Technology, Gaming, Education..."
               maxlength="60"
               required>

        <label for="content">Blog Content</label>
        <textarea id="content"
                  name="content"
                  rows="12"
                  minlength="10"
                  maxlength="10000"
                  required></textarea>

        <button type="submit">
            Publish Blog
        </button>

    </form>

    <p>
        <a href="${pageContext.request.contextPath}/news">
            Back to News
        </a>
    </p>
<p>
    <a href="${pageContext.request.contextPath}/blog/my-posts">
        View My Posts
    </a>
</p>
</main>
</body>
</html>
