
<%@ page contentType="text/html; charset=UTF-8"
         pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>

<!DOCTYPE html>
<html lang="en">
<head>
    <meta charset="UTF-8">
    <meta name="viewport"
          content="width=device-width, initial-scale=1">

    <title>Revise Article - NewsWeb</title>

    <style>
        body {
            font-family: Arial, sans-serif;
            margin: 0;
            background: #f4f4f4;
        }

        main {
            max-width: 850px;
            margin: 40px auto;
            background: white;
            border-radius: 8px;
            padding: 30px;
        }

        label {
            display: block;
            margin-top: 18px;
            margin-bottom: 6px;
            font-weight: bold;
        }

        input[type="text"], textarea {
            width: 100%;
            box-sizing: border-box;
            padding: 12px;
        }

        button {
            margin-top: 20px;
            padding: 12px 20px;
            color: white;
            background: #222;
            border: none;
            border-radius: 5px;
            cursor: pointer;
        }

        .feedback {
            background: #fff1f1;
            border-left: 4px solid #ba3030;
            padding: 15px;
        }

        .error { color: red; }
    </style>
</head>

<body>
<main>

    <h1>Revise Editorial Article</h1>

    <p>
        Your revised article will be sent
        back for editor review.
    </p>

    <div class="feedback">
        <strong>Editor's Previous Feedback</strong>
        <p>
            <c:out value="${article.rejectionReason}"
                   default="No feedback provided." />
        </p>
    </div>

    <c:if test="${not empty error}">
        <p class="error">
            <c:out value="${error}" />
        </p>
    </c:if>

    <form action="${pageContext.request.contextPath}/writer/revise"
          method="post">

        <input type="hidden"
               name="id"
               value="<c:out value='${article._id}' />">

        <input type="hidden"
               name="csrfToken"
               value="<c:out value='${sessionScope.revisionCsrfToken}' />">

        <label for="title">Title</label>

        <input type="text"
               id="title"
               name="title"
               minlength="5"
               maxlength="200"
               value="<c:out value='${article.title}' />"
               required>

        <label for="category">Category</label>

        <input type="text"
               id="category"
               name="category"
               minlength="2"
               maxlength="60"
               value="<c:out value='${article.category}' />"
               required>

        <label for="content">Content</label>

        <textarea id="content"
                  name="content"
                  rows="15"
                  minlength="50"
                  maxlength="20000"
                  required><c:out value="${article.content}" /></textarea>

        <button type="submit">
            Resubmit for Review
        </button>
    </form>

    <p>
        <a href="${pageContext.request.contextPath}/writer/articles">
            Back to My Editorial Articles
        </a>
    </p>

</main>
</body>
</html>
