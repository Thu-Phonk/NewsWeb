
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

.blog-preview {
    border: 1px solid #ddd;
    border-radius: 8px;
    background: #fafafa;
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

.ql-toolbar.ql-snow {
    background: #fafafa;
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
        
<!-- Plain-text fallback for compatibility -->
<textarea id="content"
          name="content"
          rows="12"
          maxlength="10000"
          required></textarea>

<!-- New formatted article content -->
<input type="hidden"
       id="contentHtml"
       name="contentHtml">

<!-- Quill editor appears here when JavaScript loads -->
<div id="richEditor" hidden></div>


<h2>Live Preview</h2>

<section class="blog-preview" aria-live="polite">
    <h2 id="previewTitle">Your blog title</h2>

    <p class="preview-category"
       id="previewCategory">
        Category
    </p>

    <div id="previewContent"
         class="preview-content">
        Your blog content will appear here.
    </div>
</section>

        
<div class="blog-actions">

    <button type="submit"
            name="action"
            value="draft"
            formnovalidate
            style="background:#666; color:white;">
        Save Draft
    </button>

    <button type="submit"
            name="action"
            value="submit"
            style="background:#222; color:white;">
        Submit for Review
    </button>

</div>

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

<script
    src="https://cdn.jsdelivr.net/npm/quill@2.0.3/dist/quill.js"
    defer></script>

<script
    src="${pageContext.request.contextPath}/js/blog-rich-editor.js?v=2"
    defer></script>

</body>
</html>
