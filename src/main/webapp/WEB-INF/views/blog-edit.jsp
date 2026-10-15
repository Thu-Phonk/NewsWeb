
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
<link
    href="https://cdn.jsdelivr.net/npm/quill@2.0.3/dist/quill.snow.css"
    rel="stylesheet">
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
    white-space: pre-wrap;
    overflow-wrap: anywhere;
    line-height: 1.8;
}

#richEditor {
    background: white;
}

#richEditor .ql-editor {
    min-height: 250px;
    font-size: 16px;
    line-height: 1.7;
}

.blog-preview {
    background: #fafafa;
    border: 1px solid #ddd;
    border-radius: 8px;
    padding: 24px;
    margin-top: 16px;
}

.preview-content.ql-editor {
    min-height: 0;
    padding: 0;
    line-height: 1.8;
    overflow-wrap: anywhere;
}

.preview-content.ql-editor p {
    margin-bottom: 12px;
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
        
<!-- Plain-text compatibility -->
<textarea id="content"
          name="content"
          minlength="10"
          maxlength="10000"
          required><c:out value="${blog.content}" /></textarea>

<!-- Formatted content submitted to Java -->
<input type="hidden"
       id="contentHtml"
       name="contentHtml">

<!-- Previously saved HTML, escaped for safe transport -->
<textarea id="initialContentHtml"
          hidden><c:out value="${initialContentHtml}" /></textarea>

<!-- Quill editor -->
<div id="richEditor" hidden></div>


<h2>Live Preview</h2>

<section class="blog-preview">
    <h2 id="previewTitle">Your blog title</h2>

    <p id="previewCategory"
       class="preview-category">
        Category
    </p>

    <div id="previewContent"
         class="preview-content ql-editor">
        Your blog content will appear here.
    </div>
</section>

        
<div class="blog-actions">

    <c:if test="${blog.status eq 'draft'}">

        <button type="submit"
                name="action"
                value="draft"
                formnovalidate
                style="background:#666; color:white;">
            Save Draft
        </button>

    </c:if>

    <button type="submit"
            name="action"
            value="submit"
            style="background:#222; color:white;">
        Submit for Review
    </button>

</div>

    </form>

    <p>
        <a href="${pageContext.request.contextPath}/blog/my-posts">
            Cancel and Return to My Posts
        </a>
    </p>

</main>

<script
    src="https://cdn.jsdelivr.net/npm/quill@2.0.3/dist/quill.js"
    defer></script>

<script
    src="${pageContext.request.contextPath}/js/blog-rich-editor.js?v=2"
    defer></script>

</body>
</html>
