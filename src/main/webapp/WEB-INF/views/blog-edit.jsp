
<%@ page contentType="text/html; charset=UTF-8"
         pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>

<!DOCTYPE html>
<html lang="en">
<head>
    <meta charset="UTF-8">
    <meta name="viewport"
          content="width=device-width, initial-scale=1">

    <title>Edit Blog - NewsWeb</title>

    <style>
        body {
            font-family: Arial, sans-serif;
            background: #f4f4f4;
            margin: 0;
        }

        main {
            max-width: 800px;
            margin: 40px auto;
            padding: 25px;
            background: white;
            border-radius: 8px;
        }

        label {
            display: block;
            margin-top: 15px;
            margin-bottom: 6px;
            font-weight: bold;
        }

        input[type="text"], textarea {
            box-sizing: border-box;
            width: 100%;
            padding: 10px;
        }

        textarea {
            min-height: 250px;
        }

        button {
            margin-top: 20px;
            padding: 12px 20px;
            cursor: pointer;
        }

        .error {
            color: red;
        }
    </style>
</head>

<body>
<main>

    <h1>Edit Blog Post</h1>

    <c:if test="${not empty error}">
        <p class="error">
            <c:out value="${error}" />
        </p>
    </c:if>

    <form action="${pageContext.request.contextPath}/blog/edit"
          method="post">

        <input type="hidden"
               name="id"
               value="<c:out value='${blog._id}' />">

        <input type="hidden"
               name="csrfToken"
               value="<c:out value='${sessionScope.blogEditCsrfToken}' />">

        <label for="title">Title</label>
        <input type="text"
               id="title"
               name="title"
               minlength="3"
               maxlength="150"
               value="<c:out value='${blog.title}' />"
               required>

        <label for="category">Category</label>
        <input type="text"
               id="category"
               name="category"
               minlength="2"
               maxlength="60"
               value="<c:out value='${blog.category}' />"
               required>

        <label for="content">Content</label>
        <textarea id="content"
                  name="content"
                  minlength="10"
                  maxlength="10000"
                  required><c:out value="${blog.content}" /></textarea>

        <button type="submit">
            Save Changes
        </button>

    </form>

    <p>
        <a href="${pageContext.request.contextPath}/blog/my-posts">
            Cancel and Return to My Posts
        </a>
    </p>

</main>
</body>
</html>
