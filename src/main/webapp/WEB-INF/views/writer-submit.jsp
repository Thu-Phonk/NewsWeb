
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
<link
    href="https://cdn.jsdelivr.net/npm/quill@2.0.3/dist/quill.snow.css"
    rel="stylesheet">
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

.navigation-actions {
    display: flex;
    flex-wrap: wrap;
    gap: 12px;
    margin-top: 25px;
    padding-top: 20px;
    border-top: 1px solid #ddd;
}

.nav-button {
    display: inline-block;
    padding: 12px 18px;
    background: #333;
    color: white;
    text-decoration: none;
    border-radius: 5px;
}

.nav-button:hover {
    background: #555;
}

.blog-preview {
    background: #fafafa;
    border: 1px solid #ddd;
    border-radius: 8px;
    padding: 24px;
    margin-top: 16px;
}

.blog-preview h2 {
    margin-top: 0;
    overflow-wrap: anywhere;
}

.preview-category {
    color: #777;
    font-size: 14px;
}


.preview-content {
    overflow-wrap: anywhere;
    line-height: 1.8;
}

#richEditor {
    background: white;
}

#richEditor .ql-editor {
    min-height: 300px;
    font-size: 16px;
    line-height: 1.8;
}

.ql-toolbar.ql-snow {
    background: #fafafa;
}

.preview-content.ql-editor {
    min-height: 0;
    padding: 0;
    white-space: normal;
}

.preview-content.ql-editor p {
    margin-bottom: 12px;
}


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
       value="<c:out value='${formTitle}' />"
       required>
        <input type="text"
       id="category"
       name="category"
       maxlength="60"
       required>

value="<c:out value='${formCategory}' />"
<label for="content">Article Content</label>

<!-- Plain-text compatibility and fallback -->
<textarea id="content"
          name="content"
          rows="15"
          minlength="50"
          maxlength="20000"
          required><c:out value="${formContent}" /></textarea>

<!-- Formatted HTML submitted to the Servlet -->
<input type="hidden"
       id="contentHtml"
       name="contentHtml">

<!-- Restore formatting after validation errors -->
<textarea id="initialContentHtml"
          hidden><c:out value="${formContentHtml}" /></textarea>

<!-- Rich-text editing area -->
<div id="richEditor" hidden></div>

<h2>Live Preview</h2>

<section class="blog-preview">

    <h2 id="previewTitle">
        Your article title
    </h2>

    <p id="previewCategory"
       class="preview-category">
        Category
    </p>

    <div id="previewContent"
     class="preview-content ql-editor">
        Your article content will appear here.
    </div>

</section>

        
<div class="editor-actions">

    <button type="submit"
            name="action"
            value="draft"
            formnovalidate
            style="background:#666;">
        Save Draft
    </button>

    <button type="submit"
            name="action"
            value="submit">
        Submit for Review
    </button>

</div>


    </form>

<div class="navigation-actions">

    <a class="nav-button"
       href="${pageContext.request.contextPath}/writer/articles">
        &larr; My Articles &amp; Drafts
    </a>

    <a class="nav-button"
       href="${pageContext.request.contextPath}/writer">
        Writer Dashboard
    </a>

    <a class="nav-button"
       href="${pageContext.request.contextPath}/editorials">
        Published Editorials
    </a>

</div>

</main>

<script
    src="https://cdn.jsdelivr.net/npm/quill@2.0.3/dist/quill.js"
    defer></script>

<script
    src="${pageContext.request.contextPath}/js/blog-rich-editor.js?v=3"
    defer></script>

</body>
</html>
