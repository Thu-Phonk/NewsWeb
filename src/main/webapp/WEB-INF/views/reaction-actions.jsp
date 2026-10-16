<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<section id="reactions" style="background:white;border-radius:8px;padding:18px;margin:20px 0;">
    <p><strong><c:out value="${reactionLikeCount}" /></strong> likes</p>
    <c:choose>
        <c:when test="${not empty reactionUser}">
            <div style="display:flex;flex-wrap:wrap;gap:12px;align-items:center;">
                <form action="${pageContext.request.contextPath}/reaction" method="post">
                    <input type="hidden" name="targetType" value="<c:out value='${reactionTargetType}' />">
                    <input type="hidden" name="targetId" value="<c:out value='${reactionTargetId}' />">
                    <input type="hidden" name="csrfToken" value="<c:out value='${sessionScope.reactionCsrfToken}' />">
                    <button type="submit" name="action" value="${reactionLiked ? 'unlike' : 'like'}">
                        <c:choose><c:when test="${reactionLiked}">Unlike</c:when><c:otherwise>Like</c:otherwise></c:choose>
                    </button>
                </form>
                <form action="${pageContext.request.contextPath}/reaction" method="post">
                    <input type="hidden" name="targetType" value="<c:out value='${reactionTargetType}' />">
                    <input type="hidden" name="targetId" value="<c:out value='${reactionTargetId}' />">
                    <input type="hidden" name="csrfToken" value="<c:out value='${sessionScope.reactionCsrfToken}' />">
                    <button type="submit" name="action" value="${reactionBookmarked ? 'unbookmark' : 'bookmark'}">
                        <c:choose><c:when test="${reactionBookmarked}">Remove Bookmark</c:when><c:otherwise>Bookmark</c:otherwise></c:choose>
                    </button>
                </form>
                <a href="${pageContext.request.contextPath}/my-bookmarks">My Bookmarks</a>
            </div>
        </c:when>
        <c:otherwise>
            <p><a href="${pageContext.request.contextPath}/login">Log in</a> to like or bookmark this post.</p>
        </c:otherwise>
    </c:choose>
</section>
