
<%@ page contentType="text/html; charset=UTF-8"
         pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>

<!DOCTYPE html>
<html lang="en">

<head>
    <meta charset="UTF-8">
    <meta name="viewport"
          content="width=device-width, initial-scale=1">

    <title>
        <c:out value="${article.title}" /> - NewsWeb
    </title>

    <style>
        body {
            font-family: Arial, sans-serif;
            background: #f4f4f4;
            margin: 0;
            color: #222;
        }

        header {
            background: #222;
            color: white;
            padding: 24px;
            text-align: center;
        }

        header h1 {
            margin: 0 0 12px;
        }

        header nav {
            margin-top: 18px;
        }

        header nav a {
            color: white;
            margin: 0 12px;
            text-decoration: none;
        }

        header nav a:hover {
            text-decoration: underline;
        }

        main {
            max-width: 850px;
            margin: 30px auto;
            padding: 0 16px;
        }

        .article {
            background: white;
            padding: 35px;
            border-radius: 10px;
            box-shadow: 0 2px 8px rgba(0,0,0,0.05);
        }

        .article-title {
            font-size: 34px;
            line-height: 1.25;
            margin-top: 0;
        }

        .category {
            color: #777;
            font-size: 14px;
            text-transform: uppercase;
        }

        .author {
            font-size: 14px;
            color: #555;
        }

        .divider {
            border: none;
            border-top: 1px solid #ddd;
            margin: 24px 0;
        }

        
.article-content {
    font-size: 17px;
    line-height: 1.9;
    overflow-wrap: anywhere;
}

.article-content p {
    margin: 0 0 16px;
}

.article-content h2 {
    font-size: 27px;
    margin: 28px 0 14px;
    line-height: 1.4;
}

.article-content h3 {
    font-size: 22px;
    margin: 24px 0 12px;
    line-height: 1.4;
}

.article-content ul {
    list-style-type: disc;
    padding-left: 30px;
    margin-bottom: 18px;
}

.article-content ol {
    list-style-type: decimal;
    padding-left: 30px;
    margin-bottom: 18px;
}

.article-content blockquote {
    border-left: 4px solid #777;
    background: #f7f7f7;
    margin: 20px 0;
    padding: 12px 20px;
    font-style: italic;
}

.article-content a {
    color: #1565c0;
    text-decoration: underline;
}

.plain-content {
    white-space: pre-wrap;
}


        .back-link {
            display: inline-block;
            margin-top: 30px;
            background: #222;
            color: white;
            padding: 12px 20px;
            border-radius: 5px;
            text-decoration: none;
        }

        @media (max-width: 600px) {
            .article {
                padding: 22px;
            }

            .article-title {
                font-size: 26px;
            }
        }

.article-cover {
    display: block;
    width: 100%;
    aspect-ratio: 16 / 9;
    max-height: 450px;
    object-fit: cover;
    border-radius: 8px;
    margin-bottom: 25px;
}

.comments-section {
    background: white;
    border-radius: 10px;
    padding: 30px;
    margin-top: 25px;
}

.comments-section h2 {
    margin-top: 0;
}

.comment-card {
    padding: 18px 0;
    border-bottom: 1px solid #ddd;
}

.comment-body {
    margin-top: 10px;
    line-height: 1.7;
    white-space: pre-wrap;
    overflow-wrap: anywhere;
}

.comment-date,
.comment-muted {
    color: #777;
    font-size: 14px;
}

.comment-success {
    color: #237a39;
    font-weight: bold;
}

.comments-section textarea {
    display: block;
    width: 100%;
    box-sizing: border-box;
    padding: 12px;
    margin-top: 10px;
    border: 1px solid #ccc;
    border-radius: 6px;
    font: inherit;
    resize: vertical;
}

.comment-submit {
    margin-top: 12px;
    padding: 12px 22px;
    background: #222;
    color: white;
    border: none;
    border-radius: 5px;
    cursor: pointer;
}

.comment-submit:hover {
    background: #444;
}

.comment-edit {
    margin-top: 12px;
}

.comment-edit summary {
    cursor: pointer;
    color: #1565c0;
    font-size: 14px;
}

.delete-comment {
    background: transparent;
    color: #ba3030;
    border: none;
    padding: 8px 0;
    cursor: pointer;
    margin-top: 8px;
}

.delete-comment:hover {
    text-decoration: underline;
}

.hidden-comment-label {
    color: #ba3030;
    background: #fff1f1;
    border-left: 4px solid #ba3030;
    padding: 10px 14px;
    font-size: 14px;
    font-weight: bold;
}

.moderation-controls {
    margin-top: 18px;
    padding: 15px;
    border: 1px solid #ddd;
    border-radius: 6px;
    background: #fafafa;
}

.moderation-controls label {
    display: block;
    font-size: 14px;
    font-weight: bold;
    margin-bottom: 8px;
}

.moderation-controls textarea {
    width: 100%;
    box-sizing: border-box;
    min-height: 65px;
    padding: 10px;
}

.hide-comment,
.restore-comment {
    padding: 10px 18px;
    border: none;
    border-radius: 5px;
    color: white;
    cursor: pointer;
    margin-top: 10px;
}

.hide-comment {
    background: #ba3030;
}

.restore-comment {
    background: #237a39;
}

.reply-form {
    margin-top: 15px;
}

.reply-form summary {
    color: #1565c0;
    cursor: pointer;
    font-weight: bold;
}

.comment-replies {
    margin: 10px 0 24px 35px;
    padding-left: 18px;
    border-left: 3px solid #ddd;
}

.reply-card {
    background: #fafafa;
    padding: 15px;
    margin-bottom: 12px;
    border-radius: 6px;
}

@media (max-width: 600px) {
    .comment-replies {
        margin-left: 12px;
        padding-left: 12px;
    }
}

    </style>
</head>

<body>

<header>
    <h1>NewsWeb</h1>
    <p>Editorial Articles</p>

    <nav>
        <a href="${pageContext.request.contextPath}/news">
            Latest News
        </a>

        <a href="${pageContext.request.contextPath}/editorials">
            Editorials
        </a>

        <a href="${pageContext.request.contextPath}/blogs">
            Community Blogs
        </a>
    </nav>
</header>

<main>

    
<article class="article">

    <%-- Published article cover image --%>
    <c:if test="${not empty article.coverImageUrl}">
        <img
            src="<c:out value='${article.coverImageUrl}' />"
            alt="Editorial article cover"
            class="article-cover"
            loading="lazy">
    </c:if>

    <p class="category">
        <c:out value="${article.category}" />
    </p>


        <h1 class="article-title">
            <c:out value="${article.title}" />
        </h1>

        <p class="author">
            By
            <strong>
                <c:out value="${article.authorName}" />
            </strong>
        </p>

        <hr class="divider">

        
<div class="article-content">

    <c:choose>

        <%-- New articles: render sanitized formatted HTML --%>
        <c:when test="${not empty article.safeContentHtml}">
            <c:out value="${article.safeContentHtml}"
                   escapeXml="false" />
        </c:when>

        <%-- Older articles: preserve escaped plain text --%>
        <c:otherwise>
            <div class="plain-content">
                <c:out value="${article.content}" />
            </div>
        </c:otherwise>

    </c:choose>

</div>


        <a class="back-link"
           href="${pageContext.request.contextPath}/editorials">
            &larr; Back to Editorial Articles
        </a>

    </article>
<%-- Likes and Bookmarks --%>
<jsp:include page="/WEB-INF/views/reaction-actions.jsp" />
<section id="comments" class="comments-section">

    <h2>Discussion</h2>

<c:if test="${isCommentModerator
              and param.moderated eq 'hidden'}">

    <p class="comment-success">
        Comment hidden successfully.
    </p>

</c:if>

<c:if test="${isCommentModerator
              and param.moderated eq 'restored'}">

    <p class="comment-success">
        Comment restored successfully.
    </p>

</c:if>

    <c:if test="${param.comment eq 'posted'}">
        <p class="comment-success">
            Your comment was posted successfully!
        </p>
    </c:if>

<c:if test="${param.comment eq 'edited'}">
    <p class="comment-success">
        Comment updated successfully!
    </p>
</c:if>

<c:if test="${param.comment eq 'deleted'}">
    <p class="comment-success">
        Comment deleted successfully!
    </p>
</c:if>

    <h3>Recent Comments</h3>

    <c:choose>

        <c:when test="${empty comments}">
            <p class="comment-muted">
                No comments yet. Be the first to comment!
            </p>
        </c:when>

        <c:otherwise>

            <c:forEach var="comment" items="${comments}">

                <div class="comment-card">

<c:if test="${comment.status eq 'hidden'}">

    <p class="hidden-comment-label">
        Hidden Comment — Visible to Moderators Only
    </p>

    <c:if test="${not empty comment.moderationReason}">
        <p class="comment-muted">
            Reason:
            <c:out value="${comment.moderationReason}" />
        </p>
    </c:if>

</c:if>

                    <strong>
                        <c:out value="${comment.authorName}" />
                    </strong>

                    <div class="comment-body">
                        <c:out value="${comment.body}" />
                    </div>

<%-- Reply to a visible top-level comment --%>
<c:if test="${not empty commentUser
              and comment.status eq 'visible'}">

    <details class="reply-form">
        <summary>Reply</summary>

        <form
            action="${pageContext.request.contextPath}/editorial/comment"
            method="post">

            <input type="hidden"
                   name="articleId"
                   value="<c:out value='${article._id}' />">

            <input type="hidden"
                   name="parentCommentId"
                   value="<c:out value='${comment._id}' />">

            <input type="hidden"
                   name="csrfToken"
                   value="<c:out value='${sessionScope.editorialCommentCsrfToken}' />">

            <textarea
                name="body"
                rows="3"
                minlength="2"
                maxlength="2000"
                placeholder="Write a reply..."
                required></textarea>

            <button type="submit"
                    class="comment-submit">
                Post Reply
            </button>

        </form>
    </details>

</c:if>



<c:if test="${comment.canManage}">

    <details class="comment-edit">
        <summary>Edit Comment</summary>

        <form
            action="${pageContext.request.contextPath}/editorial/comment/manage"
            method="post">

            <input type="hidden"
                   name="articleId"
                   value="<c:out value='${article._id}' />">

            <input type="hidden"
                   name="commentId"
                   value="<c:out value='${comment._id}' />">

            <input type="hidden"
                   name="csrfToken"
                   value="<c:out value='${sessionScope.editorialCommentCsrfToken}' />">

            <textarea name="body"
                      rows="3"
                      minlength="2"
                      maxlength="2000"
                      required><c:out value="${comment.body}" /></textarea>

            <button type="submit"
                    name="action"
                    value="edit"
                    class="comment-submit">
                Save Changes
            </button>

        </form>
    </details>

    <form
        action="${pageContext.request.contextPath}/editorial/comment/manage"
        method="post">

        <input type="hidden"
               name="articleId"
               value="<c:out value='${article._id}' />">

        <input type="hidden"
               name="commentId"
               value="<c:out value='${comment._id}' />">

        <input type="hidden"
               name="csrfToken"
               value="<c:out value='${sessionScope.editorialCommentCsrfToken}' />">

        <button type="submit"
                name="action"
                value="delete"
                class="delete-comment"
                onclick="return confirm('Delete this comment?');">
            Delete Comment
        </button>

    </form>

</c:if>

                    <p class="comment-date">
                        <c:out value="${comment.createdAt}" />
                    </p>

<c:if test="${isCommentModerator}">

    <div class="moderation-controls">

        <form
            action="${pageContext.request.contextPath}/editorial/comment/moderate"
            method="post">

            <input type="hidden"
                   name="articleId"
                   value="<c:out value='${article._id}' />">

            <input type="hidden"
                   name="commentId"
                   value="<c:out value='${comment._id}' />">

            <input type="hidden"
                   name="csrfToken"
                   value="<c:out value='${sessionScope.commentModerationCsrfToken}' />">

            <c:choose>

                <c:when test="${comment.status eq 'visible'}">

                    <label for="reason-${comment._id}">
                        Moderation Reason
                    </label>

                    <textarea
                        id="reason-${comment._id}"
                        name="moderationReason"
                        rows="2"
                        minlength="5"
                        maxlength="500"
                        placeholder="Explain why this comment should be hidden"
                        required></textarea>

                    <button type="submit"
                            name="action"
                            value="hide"
                            class="hide-comment">
                        Hide Comment
                    </button>

                </c:when>

                <c:when test="${comment.status eq 'hidden'}">

                    <button type="submit"
                            name="action"
                            value="restore"
                            class="restore-comment">
                        Restore Comment
                    </button>

                </c:when>

            </c:choose>

        </form>

    </div>

</c:if>

                </div>

<%-- Replies belonging to this comment --%>
<c:if test="${not empty comment.replies}">

    <div class="comment-replies">

        <c:forEach var="reply" items="${comment.replies}">

            <div class="reply-card">

                <c:if test="${reply.status eq 'hidden'}">
                    <p class="hidden-comment-label">
                        Hidden Reply — Moderators Only
                    </p>

                    <p class="comment-muted">
                        Reason:
                        <c:out value="${reply.moderationReason}" />
                    </p>
                </c:if>

                <strong>
                    <c:out value="${reply.authorName}" />
                </strong>

                <div class="comment-body">
                    <c:out value="${reply.body}" />
                </div>

                <p class="comment-date">
                    <c:out value="${reply.createdAt}" />
                </p>

                <%-- Reply authors can edit or delete their reply --%>
                <c:if test="${reply.canManage}">

                    <details class="comment-edit">
                        <summary>Edit Reply</summary>

                        <form
                            action="${pageContext.request.contextPath}/editorial/comment/manage"
                            method="post">

                            <input type="hidden"
                                   name="articleId"
                                   value="<c:out value='${article._id}' />">

                            <input type="hidden"
                                   name="commentId"
                                   value="<c:out value='${reply._id}' />">

                            <input type="hidden"
                                   name="csrfToken"
                                   value="<c:out value='${sessionScope.editorialCommentCsrfToken}' />">

                            <textarea name="body"
                                      rows="3"
                                      minlength="2"
                                      maxlength="2000"
                                      required><c:out value="${reply.body}" /></textarea>

                            <button type="submit"
                                    name="action"
                                    value="edit"
                                    class="comment-submit">
                                Save Reply
                            </button>
                        </form>
                    </details>

                    <form
                        action="${pageContext.request.contextPath}/editorial/comment/manage"
                        method="post">

                        <input type="hidden"
                               name="articleId"
                               value="<c:out value='${article._id}' />">

                        <input type="hidden"
                               name="commentId"
                               value="<c:out value='${reply._id}' />">

                        <input type="hidden"
                               name="csrfToken"
                               value="<c:out value='${sessionScope.editorialCommentCsrfToken}' />">

                        <button type="submit"
                                name="action"
                                value="delete"
                                class="delete-comment"
                                onclick="return confirm('Delete this reply?');">
                            Delete Reply
                        </button>
                    </form>

                </c:if>

                <%-- Moderators can hide or restore replies --%>
                <c:if test="${isCommentModerator}">

                    <div class="moderation-controls">

                        <form
                            action="${pageContext.request.contextPath}/editorial/comment/moderate"
                            method="post">

                            <input type="hidden"
                                   name="articleId"
                                   value="<c:out value='${article._id}' />">

                            <input type="hidden"
                                   name="commentId"
                                   value="<c:out value='${reply._id}' />">

                            <input type="hidden"
                                   name="csrfToken"
                                   value="<c:out value='${sessionScope.commentModerationCsrfToken}' />">

                            <c:choose>

                                <c:when test="${reply.status eq 'visible'}">

                                    <label>Moderation Reason</label>

                                    <textarea
                                        name="moderationReason"
                                        rows="2"
                                        minlength="5"
                                        maxlength="500"
                                        required></textarea>

                                    <button type="submit"
                                            name="action"
                                            value="hide"
                                            class="hide-comment">
                                        Hide Reply
                                    </button>

                                </c:when>

                                <c:otherwise>

                                    <button type="submit"
                                            name="action"
                                            value="restore"
                                            class="restore-comment">
                                        Restore Reply
                                    </button>

                                </c:otherwise>

                            </c:choose>

                        </form>
                    </div>

                </c:if>

            </div>

        </c:forEach>

    </div>

</c:if>

            </c:forEach>

        </c:otherwise>

    </c:choose>

    <h3>Leave a Comment</h3>

    <c:choose>

        <c:when test="${not empty commentUser}">

            <p class="comment-muted">
                Commenting as
                <strong>
                    <c:out value="${commentUser.username}" />
                </strong>
            </p>

            <form
                action="${pageContext.request.contextPath}/editorial/comment"
                method="post">

                <input type="hidden"
                       name="articleId"
                       value="<c:out value='${article._id}' />">

                <input type="hidden"
                       name="csrfToken"
                       value="<c:out value='${sessionScope.editorialCommentCsrfToken}' />">

                <label for="commentBody">
                    Your Comment
                </label>

                <textarea
                    id="commentBody"
                    name="body"
                    rows="4"
                    minlength="2"
                    maxlength="2000"
                    placeholder="Share your thoughts..."
                    required></textarea>

                <button type="submit"
                        class="comment-submit">
                    Post Comment
                </button>

            </form>

        </c:when>

        <c:otherwise>

            <p>
                <a href="${pageContext.request.contextPath}/login">
                    Log in
                </a>
                to join the discussion.
            </p>

        </c:otherwise>

    </c:choose>

</section>

</main>

</body>
</html>
