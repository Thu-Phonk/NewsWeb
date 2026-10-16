<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<!DOCTYPE html>
<html lang="en"><head><meta charset="UTF-8"><meta name="viewport" content="width=device-width, initial-scale=1">
<title>My Bookmarks - NewsWeb</title>
<style>
body{font-family:Arial,sans-serif;background:#f4f4f4;color:#222;margin:0}
main{max-width:850px;margin:35px auto;padding:20px}
.bookmark{background:white;padding:20px;margin-bottom:18px;border-radius:8px}
.bookmark img{max-width:100%;width:100%;max-height:250px;object-fit:cover;border-radius:6px}
a{color:#1457a5}
</style><link href="https://fonts.googleapis.com/css2?family=Inter:wght@400;500;600;700&family=Lora:wght@400;500;600;700&display=swap" rel="stylesheet">
<link rel="stylesheet" href="${pageContext.request.contextPath}/assets/gr6/navbar.css?v=1">
<link rel="stylesheet" href="${pageContext.request.contextPath}/assets/gr6/newsweb-theme.css?v=1">
</head><body>
<jsp:include page="/WEB-INF/views/partials/gr6-header.jsp" /><main>
<h1>My Bookmarks</h1>
<p><a href="${pageContext.request.contextPath}/news">News</a> · <a href="${pageContext.request.contextPath}/editorials">Editorials</a> · <a href="${pageContext.request.contextPath}/blogs">Community Blogs</a></p>
<c:choose>
<c:when test="${empty bookmarks}"><p>You have no published bookmarked posts yet.</p></c:when>
<c:otherwise><c:forEach var="item" items="${bookmarks}">
<article class="bookmark">
<c:if test="${not empty item.coverImageUrl}"><img src="<c:out value='${item.coverImageUrl}' />" alt="Bookmarked article cover" loading="lazy"></c:if>
<p><c:out value="${item.targetType}" /> · <c:out value="${item.category}" /></p>
<h2><a href="<c:out value='${item.url}' />"><c:out value="${item.title}" /></a></h2>
</article>
</c:forEach></c:otherwise>
</c:choose></main></body></html>
