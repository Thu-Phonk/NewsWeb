<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<!DOCTYPE html>
<html lang="en">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1">
    <title>Editorial Articles - NewsWeb</title>
    <style>
        body { font-family: Arial, sans-serif; background: #f4f4f4; margin: 0; color: #222; }
        header { background: #222; color: #fff; padding: 25px; text-align: center; }
        header a { color: #fff; margin: 0 10px; }
        main { max-width: 1000px; padding: 0 16px; margin: 30px auto; }
        .filters { background: white; padding: 20px; margin-bottom: 20px; border-radius: 8px; }
        .filters form { display: flex; flex-wrap: wrap; align-items: end; gap: 12px; }
        .filter-field { display: flex; flex: 1 1 180px; flex-direction: column; gap: 6px; }
        .filter-field label { font-size: 14px; font-weight: bold; }
        .filter-field input, .filter-field select { padding: 10px; font: inherit; border: 1px solid #bbb; border-radius: 5px; min-width: 0; }
        .button, .filters button { display: inline-block; padding: 10px 16px; background: #222; color: white; border: none; text-decoration: none; border-radius: 5px; cursor: pointer; font: inherit; }
        .reset-link { color: #1565c0; padding: 10px 0; }
        .summary { margin: 16px 0; color: #555; }
        .pagination { display: flex; align-items: center; justify-content: center; flex-wrap: wrap; gap: 12px; margin: 28px 0; }
        .pagination .disabled { color: #777; border: 1px solid #ddd; padding: 10px 16px; border-radius: 5px; }
        .article, .blog-card { background: white; padding: 25px; margin-bottom: 20px; border-radius: 8px; }
        .meta, .author, .category { color: #666; font-size: 14px; }
        .excerpt, .plain-content { white-space: pre-wrap; overflow-wrap: anywhere; line-height: 1.7; }
        .blog-cover { display: block; width: 100%; max-height: 380px; aspect-ratio: 16 / 9; object-fit: cover; border-radius: 8px; margin-bottom: 20px; }
        .content { line-height: 1.8; overflow-wrap: anywhere; }
        .content p { margin: 0 0 14px; }
        .content ul { list-style: disc; padding-left: 28px; }
        .content ol { list-style: decimal; padding-left: 28px; }
        .content blockquote { border-left: 4px solid #999; margin: 18px 0; padding: 8px 18px; background: #f7f7f7; font-style: italic; }
        .content a { color: #1565c0; text-decoration: underline; }
        .create-link { display: inline-block; margin-bottom: 20px; }
        @media (max-width: 600px) { .filters form { display: block; } .filter-field { margin: 0 0 12px; } }
    </style>
<link href="https://fonts.googleapis.com/css2?family=Inter:wght@400;500;600;700&family=Lora:wght@400;500;600;700&display=swap" rel="stylesheet">
<link rel="stylesheet" href="${pageContext.request.contextPath}/assets/gr6/navbar.css?v=1">
<link rel="stylesheet" href="${pageContext.request.contextPath}/assets/gr6/newsweb-theme.css?v=1">
</head>
<body>
<jsp:include page="/WEB-INF/views/partials/gr6-header.jsp" />
<main>
    <h1>Editorial Articles</h1>

    <section class="filters" aria-label="Search and filter published posts">
        <form method="get" action="${pageContext.request.contextPath}/editorials">
            <div class="filter-field">
                <label for="searchQuery">Search title</label>
                <input id="searchQuery" name="q" type="search" maxlength="100"
                       placeholder="Search titles..." value="<c:out value='${query}' />">
            </div>
            <div class="filter-field">
                <label for="category">Category</label>
                <select id="category" name="category">
                    <option value="">All categories</option>
                    <c:forEach var="categoryItem" items="${categories}">
                        <c:choose>
                            <c:when test="${selectedCategory eq categoryItem}">
                                <option value="<c:out value='${categoryItem}' />" selected="selected"><c:out value="${categoryItem}" /></option>
                            </c:when>
                            <c:otherwise>
                                <option value="<c:out value='${categoryItem}' />"><c:out value="${categoryItem}" /></option>
                            </c:otherwise>
                        </c:choose>
                    </c:forEach>
                </select>
            </div>
            <div class="filter-field">
                <label for="sort">Sort</label>
                <select id="sort" name="sort">
                    <c:choose>
                        <c:when test="${sortOrder eq 'oldest'}">
                            <option value="newest">Newest first</option>
                            <option value="oldest" selected="selected">Oldest first</option>
                        </c:when>
                        <c:otherwise>
                            <option value="newest" selected="selected">Newest first</option>
                            <option value="oldest">Oldest first</option>
                        </c:otherwise>
                    </c:choose>
                </select>
            </div>
            <button type="submit">Search</button>
            <a class="reset-link" href="${pageContext.request.contextPath}/editorials">Clear</a>
        </form>
    </section>

    <p class="summary"><strong><c:out value="${totalMatches}" /></strong> matching posts · Page <c:out value="${currentPage}" /> of <c:out value="${totalPages}" /></p>

    <c:choose>
        <c:when test="${empty articles}">
            <div class="article"><h2>No matching published posts</h2><p>Try another keyword or category.</p></div>
        </c:when>
        <c:otherwise>
        <c:forEach var="article" items="${articles}">
            <article class="article">
                <h2><c:out value="${article.title}" /></h2>
                <p class="meta">By <c:out value="${article.authorName}" /> | <c:out value="${article.category}" /></p>
                <p class="excerpt"><c:out value="${article.excerpt}" /></p>
                <c:url var="readUrl" value="/editorial"><c:param name="id" value="${article._id}" /></c:url>
                <a class="button" href="${readUrl}">Read Full Article</a>
            </article>
        </c:forEach>
        </c:otherwise>
    </c:choose>

    <nav class="pagination" aria-label="Pagination">
        <c:choose>
            <c:when test="${hasPrevious}">
                <c:url var="previousUrl" value="/editorials">
                    <c:param name="q" value="${query}" />
                    <c:param name="category" value="${selectedCategory}" />
                    <c:param name="sort" value="${sortOrder}" />
                    <c:param name="page" value="${currentPage - 1}" />
                </c:url>
                <a class="button" href="${previousUrl}">Previous</a>
            </c:when>
            <c:otherwise><span class="disabled">Previous</span></c:otherwise>
        </c:choose>
        <span>Page <strong><c:out value="${currentPage}" /></strong> / <c:out value="${totalPages}" /></span>
        <c:choose>
            <c:when test="${hasNext}">
                <c:url var="nextUrl" value="/editorials">
                    <c:param name="q" value="${query}" />
                    <c:param name="category" value="${selectedCategory}" />
                    <c:param name="sort" value="${sortOrder}" />
                    <c:param name="page" value="${currentPage + 1}" />
                </c:url>
                <a class="button" href="${nextUrl}">Next</a>
            </c:when>
            <c:otherwise><span class="disabled">Next</span></c:otherwise>
        </c:choose>
    </nav>
</main>
</body>
</html>
