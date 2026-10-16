
<%@ page contentType="text/html; charset=UTF-8"
         pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>

<!DOCTYPE html>
<html lang="en">
<head>
    <meta charset="UTF-8">
    <meta name="viewport"
          content="width=device-width, initial-scale=1">

    <title>My Editorial Articles - NewsWeb</title>

    <style>
        body {
            font-family: Arial, sans-serif;
            background: #f4f4f4;
            margin: 0;
        }

        header {
            background: #222;
            color: white;
            text-align: center;
            padding: 25px;
        }

        header a {
            color: white;
            margin: 0 10px;
        }

        main {
            max-width: 900px;
            margin: 30px auto;
        }

        .article {
            background: white;
            padding: 25px;
            margin-bottom: 20px;
            border-radius: 8px;
        }

        .meta {
            color: #666;
            font-size: 14px;
        }

        
.content {
    line-height: 1.8;
    overflow-wrap: anywhere;
}

/* Rich-text paragraphs */
.content p {
    margin: 0 0 14px;
}

/* Article headings */
.content h2 {
    font-size: 24px;
    margin: 22px 0 12px;
}

.content h3 {
    font-size: 20px;
    margin: 18px 0 10px;
}

/* Bullet lists */
.content ul {
    list-style-type: disc;
    padding-left: 28px;
}

/* Numbered lists */
.content ol {
    list-style-type: decimal;
    padding-left: 28px;
}

/* Quotes */
.content blockquote {
    border-left: 4px solid #999;
    padding: 10px 18px;
    margin: 18px 0;
    background: #f7f7f7;
    font-style: italic;
}

/* Links */
.content a {
    color: #1565c0;
    text-decoration: underline;
}

/* Preserve original plain-text formatting */
.plain-content {
    white-space: pre-wrap;
}


        .status {
            font-weight: bold;
        }

        .pending { color: #996300; }
        .published { color: #237a39; }
        .rejected { color: #ba3030; }

        .feedback {
            background: #fff1f1;
            border-left: 4px solid #ba3030;
            padding: 15px;
            margin-top: 16px;
        }

        .button {
            display: inline-block;
            padding: 10px 16px;
            background: #222;
            color: white;
            text-decoration: none;
            border-radius: 5px;
            margin: 15px 0;
        }

.editorial-cover {
    display: block;
    width: 100%;
    aspect-ratio: 16 / 9;
    max-height: 380px;
    object-fit: cover;
    border-radius: 8px;
    margin-bottom: 20px;
}

    </style>
</head>

<body>

<header>
    <h1>My Editorial Articles</h1>

    <p>
        Welcome,
        <c:out value="${writerName}" />
    </p>

    
<nav>
    <a href="${pageContext.request.contextPath}/news">
        News
    </a>

    <a href="${pageContext.request.contextPath}/editorials">
        Editorials
    </a>

    <a href="${pageContext.request.contextPath}/blogs">
        Community Blogs
    </a>

    <a href="${pageContext.request.contextPath}/writer">
        Writer Dashboard
    </a>

    <a href="${pageContext.request.contextPath}/writer/submit">
        Write Article
    </a>

    <a href="${pageContext.request.contextPath}/writer/articles">
        My Editorial Articles
    </a>
</nav>

</header>


<main>

    <c:if test="${param.updated eq 'draft'}">
        <p style="color:green;">
            Draft changes saved successfully!
        </p>
    </c:if>

    <c:if test="${param.submitted eq '1'}">
        <p style="color:green;">
            Article submitted for editorial review!
        </p>
    </c:if>

    <a class="button"
       href="${pageContext.request.contextPath}/writer/submit">
        + Submit New Article
    </a>


    
<c:choose>

    <c:when test="${empty articles}">

        <div class="article">
            <h2>No editorial articles yet</h2>

            <p>
                You haven't submitted any editorial
                articles for review.
            </p>
        </div>

    </c:when>

    <c:otherwise>

        <c:forEach var="article" items="${articles}">

            
<article class="article">

    <%-- Display the saved editorial cover --%>
    <c:if test="${not empty article.coverImageUrl}">
        <img
            src="<c:out value='${article.coverImageUrl}' />"
            alt="Article cover"
            class="editorial-cover"
            loading="lazy">
    </c:if>

    <h2>
        <c:out value="${article.title}" />
    </h2>


                <p class="meta">
                    Category:
                    <c:out value="${article.category}" />
                </p>

                <p>
                    Status:

                    <c:choose>

                        <c:when test="${article.status eq 'draft'}">
                            <span class="status"
                                  style="color:#555;">
                                Draft
                            </span>
                        </c:when>

                        <c:when test="${article.status eq 'pending'}">
                            <span class="status pending">
                                Pending Review
                            </span>
                        </c:when>

                        <c:when test="${article.status eq 'published'}">
                            <span class="status published">
                                Published
                            </span>
                        </c:when>

                        <c:when test="${article.status eq 'rejected'}">
                            <span class="status rejected">
                                Rejected
                            </span>
                        </c:when>

                        <c:otherwise>
                            <c:out value="${article.status}" />
                        </c:otherwise>

                    </c:choose>
                </p>

                
<h3>Article Content</h3>

<div class="content">

    <c:choose>

        <%-- Display sanitized rich-text formatting --%>
        <c:when test="${not empty article.safeContentHtml}">
            <c:out value="${article.safeContentHtml}"
                   escapeXml="false" />
        </c:when>

        <%-- Support older plain-text articles --%>
        <c:otherwise>
            <div class="plain-content">
                <c:out value="${article.content}" />
            </div>
        </c:otherwise>

    </c:choose>

</div>


                <!-- Draft -->
                
<c:if test="${article.status eq 'draft'}">

    <p class="meta">
        This article is saved privately
        and has not been submitted
        for editor review.
    </p>

    <c:url var="draftUrl" value="/writer/draft">
        <c:param name="id" value="${article._id}" />
    </c:url>

    <p>
        <a class="button" href="${draftUrl}">
            Edit Draft
        </a>
    </p>

</c:if>

                <!-- Pending -->
                <c:if test="${article.status eq 'pending'}">
                    <p class="meta">
                        Your article is awaiting editor review.
                    </p>
                </c:if>

                <!-- Published -->
                <c:if test="${article.status eq 'published'}">
                    <p class="meta">
                        Your article has been approved.
                        It is now visible on the News page.
                    </p>
                </c:if>

                <!-- Rejected -->
                <c:if test="${article.status eq 'rejected'}">

                    <div class="feedback">
                        <strong>Editor's Feedback</strong>

                        <p>
                            <c:out
                                value="${article.rejectionReason}"
                                default="No feedback provided." />
                        </p>
                    </div>

                    <c:url var="reviseUrl"
                           value="/writer/revise">
                        <c:param name="id"
                                 value="${article._id}" />
                    </c:url>

                    <p>
                        <a class="button" href="${reviseUrl}">
                            Revise and Resubmit
                        </a>
                    </p>

                </c:if>

            </article>

        </c:forEach>

    </c:otherwise>

</c:choose>

</main>

</body>
</html>
