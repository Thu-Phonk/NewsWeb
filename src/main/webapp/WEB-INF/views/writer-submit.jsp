
<%@ page contentType="text/html; charset=UTF-8"
         pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>

<!DOCTYPE html>
<html lang="en">

<head>
    <meta charset="UTF-8">
    <meta name="viewport"
          content="width=device-width, initial-scale=1">

    <title>Submit Article - NewsWeb</title>

    <style>
        body {
            font-family: Arial, sans-serif;
            background: #f4f4f4;
            margin: 0;
        }

        header {
            background: #222;
            color: white;
            padding: 25px;
            text-align: center;
        }

        main {
            max-width: 850px;
            margin: 30px auto;
            background: white;
            padding: 30px;
            border-radius: 8px;
        }

        label {
            display: block;
            margin-top: 18px;
            margin-bottom: 6px;
            font-weight: bold;
        }

        input, textarea {
            box-sizing: border-box;
            width: 100%;
            padding: 12px;
        }

        textarea {
            min-height: 300px;
        }

        button {
            background: #222;
            color: white;
            border: none;
            padding: 12px 22px;
            margin-top: 20px;
            cursor: pointer;
            border-radius: 5px;
        }

        .success { color: green; }
        .error { color: red; }
    </style>
</head>

<body>

<header>
    <h1>NewsWeb Writer</h1>
    <p>Editorial Article Submission</p>
</header>

<main>

    <h2>Submit a News Article</h2>

    <p>
        Your article will be reviewed by an editor
        before publication.
    </p>

    <c:if test="${param.submitted eq '1'}">
        <p class="success">
            Article submitted successfully!
            Status: Pending Review.
        </p>
    </c:if>

    <c:if test="${not empty error}">
        <p class="error">
            <c:out value="${error}" />
        </p>
    </c:if>

    <form action="${pageContext.request.contextPath}/writer/submit"
          method="post">

        <input type="hidden"
               name="csrfToken"
               value="<c:out value='${sessionScope.articleCsrfToken}' />">

        <label for="title">Article Title</label>

        <input type="text"
               id="title"
               name="title"
               minlength="5"
               maxlength="200"
               required>

        <label for="category">Category</label>

        <input type="text"
               id="category"
               name="category"
               maxlength="60"
               required>

        <label for="content">Article Content</label>

        <textarea id="content"
                  name="content"
                  rows="15"
                  minlength="50"
                  maxlength="20000"
                  required></textarea>

        <button type="submit">
            Submit for Review
        </button>

    </form>

    <p>
        <a href="${pageContext.request.contextPath}/writer">
            Back to Writer Dashboard
        </a>
    </p>

</main>

</body>
</html>
