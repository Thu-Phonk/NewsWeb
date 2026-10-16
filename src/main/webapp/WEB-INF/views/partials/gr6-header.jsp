<%@ page pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<div class="gn-utility gn-scope">
    <div class="gn-wrap">
        <span class="gn-live">GR6 NEWS &middot; Community</span>
        <span class="gn-utility-note">News &bull; Opinions &bull; Everyday Life</span>
    </div>
</div>
<div class="gn-scope">
    <header class="gn-masthead">
        <div class="gn-wrap">
            <div class="gn-mast-left"><span class="gn-date">Independent perspectives</span></div>
            <a href="${pageContext.request.contextPath}/news" class="gn-logo" aria-label="GR6 News Home">
                <span class="gn-logo-text">GR6 <b>NEWS</b></span>
                <span class="gn-tagline">NEWS &middot; OPINIONS &middot; LIFE</span>
            </a>
            <div class="gn-mast-right">
                <c:choose>
                    <c:when test="${not empty sessionScope.userId}">
                        <span class="gn-user" title="Signed in">
                            <span class="gn-avatar">G</span>
                            <span class="gn-account-name"><c:out value="${sessionScope.username}" /></span>
                        </span>
                        <form action="${pageContext.request.contextPath}/logout" method="post" class="gn-logout-form">
                            <input type="hidden" name="csrfToken" value="<c:out value='${sessionScope.logoutCsrfToken}' />">
                            <button class="gn-signin gn-logout-button" type="submit">Sign out</button>
                        </form>
                    </c:when>
                    <c:otherwise>
                        <a href="${pageContext.request.contextPath}/login" class="gn-signin">Sign in</a>
                    </c:otherwise>
                </c:choose>
            </div>
        </div>
    </header>
</div>
<nav class="gn-catbar gn-scope" aria-label="Main navigation">
    <div class="gn-wrap">
        <ul class="gn-cats">
            <li class="gn-cat"><a class="gn-cat-link" href="${pageContext.request.contextPath}/news">Home</a></li>
            <li class="gn-cat"><a class="gn-cat-link" href="${pageContext.request.contextPath}/editorials">Editorials</a></li>
            <li class="gn-cat"><a class="gn-cat-link" href="${pageContext.request.contextPath}/blogs">Community Blogs</a></li>
            <c:if test="${not empty sessionScope.userId}">
                <li class="gn-cat"><a class="gn-cat-link" href="${pageContext.request.contextPath}/my-bookmarks">Bookmarks</a></li>
                <li class="gn-cat"><a class="gn-cat-link" href="${pageContext.request.contextPath}/blog/my-posts">My Posts</a></li>
                <li class="gn-cat"><a class="gn-cat-link" href="${pageContext.request.contextPath}/blog/create">Write a Blog</a></li>
            </c:if>
            <c:if test="${sessionScope.role eq 'writer'}">
                <li class="gn-cat"><a class="gn-cat-link" href="${pageContext.request.contextPath}/writer">Writer Desk</a></li>
            </c:if>
            <c:if test="${sessionScope.role eq 'editor'}">
                <li class="gn-cat"><a class="gn-cat-link" href="${pageContext.request.contextPath}/editor">Editor Desk</a></li>
            </c:if>
            <c:if test="${sessionScope.role eq 'administrator'}">
                <li class="gn-cat"><a class="gn-cat-link" href="${pageContext.request.contextPath}/admin/users">Admin Desk</a></li>
            </c:if>
        </ul>
    </div>
</nav>
