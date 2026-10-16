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

    <link
        href="https://cdn.jsdelivr.net/npm/quill@2.0.3/dist/quill.snow.css"        
        rel="stylesheet">

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

        #content {
            min-height: 250px;
        }

        #richEditor {
            background: white;
        }

        #richEditor .ql-editor {
            min-height: 300px;
            font-size: 16px;
            line-height: 1.8;
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
            border-radius: 5px;
        }

        .error {
            color: #b00020;
        }

        .blog-preview {
            background: #fafafa;
            border: 1px solid #ddd;
            border-radius: 8px;
            padding: 24px;
            margin-top: 20px;
        }

        .preview-category {
            color: #666;
            font-size: 14px;
        }

        .preview-content.ql-editor {
            min-height: 0;
            padding: 0;
            white-space: normal;
            line-height: 1.8;
            overflow-wrap: anywhere;
        }

        .navigation {
            margin-top: 25px;
        }
    
/* Editorial cover preview */
.preview-cover {
    display: block;
    width: 100%;
    aspect-ratio: 16 / 9;
    max-height: 350px;
    object-fit: cover;
    border-radius: 8px;
    margin-bottom: 20px;
}
.preview-cover[hidden] { display: none; }
.cover-choice { width: auto; }

</style>
</head>

<body>
<main>

    <h1>Revise Editorial Article</h1>

    <p>
        Edit your rejected article and submit it
        for another editorial review.
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
          method="post"
          enctype="multipart/form-data">

        <input type="hidden"
               name="id"
               value="<c:out value='${article._id}' />">

        <input type="hidden"
               name="csrfToken"
               value="<c:out value='${sessionScope.revisionCsrfToken}' />">        

        <label for="title">Article Title</label>

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

        <label for="coverImage">Cover Image (Optional)</label>
<input id="coverImage"
       name="coverImage"
       type="file"
       accept="image/jpeg,image/png">
<p style="font-size:13px;color:#666;">
    JPEG or PNG, maximum 5 MB. Leave empty to keep your current image.
</p>
<c:if test="${not empty article.coverImageUrl}">
    <label>
        <input class="cover-choice" type="checkbox"
               id="removeCover" name="removeCover" value="true">
        Remove current cover
    </label>
</c:if>

<label for="content">Article Content</label>

        <!-- Plain-text fallback -->
        <textarea id="content"
                  name="content"
                  rows="15"
                  minlength="50"
                  maxlength="20000"
                  required><c:out value="${article.content}" /></textarea>

        <!-- Formatted content for Java -->
        <input type="hidden"
               id="contentHtml"
               name="contentHtml">

        <!-- Existing sanitized formatting -->
        <textarea id="initialContentHtml"
                  hidden><c:out value="${initialContentHtml}" /></textarea>        

        <!-- Quill rich-text editor -->
        <div id="richEditor" hidden></div>

        <h2>Live Preview</h2>

        <section class="blog-preview">
<c:choose>
    <c:when test="${not empty article.coverImageUrl}">
        <img id="coverPreview" class="preview-cover"
             src="<c:out value='${article.coverImageUrl}' />"
             alt="Article cover preview">
    </c:when>
    <c:otherwise>
        <img id="coverPreview" class="preview-cover"
             alt="Article cover preview" hidden>
    </c:otherwise>
</c:choose>


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

        <button type="submit">
            Resubmit for Review
        </button>

    </form>

    <div class="navigation">
        <a href="${pageContext.request.contextPath}/writer/articles">
            &larr; Back to My Editorial Articles
        </a>
    </div>

</main>

<script
    src="https://cdn.jsdelivr.net/npm/quill@2.0.3/dist/quill.js"
    defer></script>

<script
    src="${pageContext.request.contextPath}/js/blog-rich-editor.js?v=3"
    defer></script>

<script src="${pageContext.request.contextPath}/js/editorial-cover-preview.js?v=1"
        defer></script>
</body>
</html>
