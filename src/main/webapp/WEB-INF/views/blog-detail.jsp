<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<!DOCTYPE html>
<html lang="en">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1">
    <title><c:out value="${blog.title}" /> - NewsWeb</title>
    <style>
        body { margin: 0; background: #f4f4f4; color: #222; font-family: Arial, sans-serif; }
        header { background: #222; color: white; text-align: center; padding: 25px; }
        header a { color: white; margin: 0 10px; }
        main { max-width: 850px; margin: 28px auto; padding: 0 16px; }
        .panel { background: white; border-radius: 10px; padding: 30px; margin-bottom: 22px; }
        .cover { width: 100%; aspect-ratio: 16 / 9; max-height: 440px; object-fit: cover; border-radius: 8px; }
        .meta, .muted { color: #666; font-size: 14px; }
        .content { line-height: 1.8; overflow-wrap: anywhere; }
        .content p { margin-bottom: 14px; }
        .content ul { list-style: disc; padding-left: 28px; }
        .content ol { list-style: decimal; padding-left: 28px; }
        .content blockquote { border-left: 4px solid #999; background: #f7f7f7; padding: 10px 18px; }
        .content a { color: #1565c0; }
        .plain-content, .comment-body { white-space: pre-wrap; overflow-wrap: anywhere; }
        .comment-card { padding: 18px 0; border-bottom: 1px solid #ddd; }
        .reply-list { margin: 10px 0 16px 32px; padding-left: 16px; border-left: 3px solid #ddd; }
        .reply-card { background: #fafafa; padding: 15px; margin: 12px 0; border-radius: 6px; }
        .comment-body { margin-top: 10px; line-height: 1.6; }
        .hidden-label { color: #9a2020; font-weight: bold; }
        .moderation-controls { margin-top: 14px; background: #f8f8f8; padding: 12px; border-radius: 6px; }
        textarea { display: block; width: 100%; box-sizing: border-box; padding: 10px; margin: 10px 0; font: inherit; }
        button { background: #222; color: white; border: none; border-radius: 5px; cursor: pointer; padding: 10px 16px; margin: 7px 6px 7px 0; }
        .button-danger { background: #ad3030; }
        details { margin-top: 12px; }
        summary { color: #1565c0; cursor: pointer; }
        .notice { color: #237a39; }
        label { font-weight: bold; font-size: 14px; }
        @media (max-width: 600px) { .panel { padding: 18px; } .reply-list { margin-left: 12px; padding-left: 12px; } }
    </style>
<link href="https://fonts.googleapis.com/css2?family=Inter:wght@400;500;600;700&family=Lora:wght@400;500;600;700&display=swap" rel="stylesheet">
<link rel="stylesheet" href="${pageContext.request.contextPath}/assets/gr6/navbar.css?v=1">
<link rel="stylesheet" href="${pageContext.request.contextPath}/assets/gr6/newsweb-theme.css?v=1">
</head>
<body>
<jsp:include page="/WEB-INF/views/partials/gr6-header.jsp" />
<main>
    <article class="panel">
        <c:if test="${not empty blog.coverImageUrl}">
            <img class="cover" src="<c:out value='${blog.coverImageUrl}' />" alt="Blog cover" loading="lazy">
        </c:if>
        <h1><c:out value="${blog.title}" /></h1>
        <p class="meta">Category: <c:out value="${blog.category}" /> | By <c:out value="${blog.authorName}" /></p>
        <div class="content">
            <c:choose>
                <c:when test="${not empty blog.safeContentHtml}">
                    <%-- The Servlet sanitized this value using RichTextUtil. --%>
                    <c:out value="${blog.safeContentHtml}" escapeXml="false" />
                </c:when>
                <c:otherwise>
                    <div class="plain-content"><c:out value="${blog.content}" /></div>
                </c:otherwise>
            </c:choose>
        </div>
    </article>
    <%-- Blog Likes and Bookmarks --%>
    <jsp:include page="/WEB-INF/views/reaction-actions.jsp" />
    <section id="comments" class="panel">
        <h2>Discussion</h2>
        <c:if test="${param.comment eq 'posted'}"><p class="notice">Comment posted successfully.</p></c:if>
        <c:if test="${param.comment eq 'edited'}"><p class="notice">Comment updated successfully.</p></c:if>
        <c:if test="${param.comment eq 'deleted'}"><p class="notice">Comment deleted.</p></c:if>
        <c:if test="${isCommentModerator and param.moderated eq 'hidden'}"><p class="notice">Comment hidden.</p></c:if>
        <c:if test="${isCommentModerator and param.moderated eq 'restored'}"><p class="notice">Comment restored.</p></c:if>

        <h3>Recent Comments</h3>
        <c:choose>
            <c:when test="${empty comments}"><p class="muted">No comments yet.</p></c:when>
            <c:otherwise>
                <c:forEach var="comment" items="${comments}">
                    <div class="comment-card">
                        <c:if test="${comment.status eq 'hidden'}">
                            <p class="hidden-label">Hidden comment (moderators only)</p>
                            <p class="muted">Reason: <c:out value="${comment.moderationReason}" /></p>
                        </c:if>
                        <strong><c:out value="${comment.authorName}" /></strong>
                        <p class="muted"><c:out value="${comment.createdAt}" /></p>
                        <div class="comment-body"><c:out value="${comment.body}" /></div>

                        <c:if test="${not empty commentUser and comment.status eq 'visible'}">
                            <details>
                                <summary>Reply</summary>
                                <form action="${pageContext.request.contextPath}/blog/comment" method="post">
                                    <input type="hidden" name="blogId" value="<c:out value='${blog._id}' />">
                                    <input type="hidden" name="parentCommentId" value="<c:out value='${comment._id}' />">
                                    <input type="hidden" name="csrfToken" value="<c:out value='${sessionScope.blogCommentCsrfToken}' />">
                                    <textarea name="body" rows="3" minlength="2" maxlength="2000" required placeholder="Write a reply..."></textarea>
                                    <button type="submit">Post Reply</button>
                                </form>
                            </details>
                        </c:if>
                        <c:set var="entry" value="${comment}" />
                        <%@ include file="blog-comment-actions.jspf" %>
                    </div>

                    <c:if test="${not empty comment.replies}">
                        <div class="reply-list">
                            <c:forEach var="reply" items="${comment.replies}">
                                <div class="reply-card">
                                    <c:if test="${reply.status eq 'hidden'}">
                                        <p class="hidden-label">Hidden reply (moderators only)</p>
                                        <p class="muted">Reason: <c:out value="${reply.moderationReason}" /></p>
                                    </c:if>
                                    <strong><c:out value="${reply.authorName}" /></strong>
                                    <p class="muted"><c:out value="${reply.createdAt}" /></p>
                                    <div class="comment-body"><c:out value="${reply.body}" /></div>
                                    <c:set var="entry" value="${reply}" />
                                    <%@ include file="blog-comment-actions.jspf" %>
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
                <p class="muted">Commenting as <strong><c:out value="${commentUser.username}" /></strong></p>
                <form action="${pageContext.request.contextPath}/blog/comment" method="post">
                    <input type="hidden" name="blogId" value="<c:out value='${blog._id}' />">
                    <input type="hidden" name="csrfToken" value="<c:out value='${sessionScope.blogCommentCsrfToken}' />">
                    <label for="commentBody">Your Comment</label>
                    <textarea id="commentBody" name="body" rows="4" minlength="2" maxlength="2000" required placeholder="Share your thoughts..."></textarea>
                    <button type="submit">Post Comment</button>
                </form>
            </c:when>
            <c:otherwise>
                <p><a href="${pageContext.request.contextPath}/login">Log in</a> to join the discussion.</p>
            </c:otherwise>
        </c:choose>
    </section>
</main>
</body>
</html>
