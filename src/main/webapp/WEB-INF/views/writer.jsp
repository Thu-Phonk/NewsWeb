
<%@ page contentType="text/html; charset=UTF-8"
         pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>

<!DOCTYPE html>
<html lang="en">

<head>
    <meta charset="UTF-8">
    <title>Writer Dashboard - NewsWeb</title>

    <style>
        body {
            font-family: Arial, sans-serif;
            background: #f4f4f4;
            margin: 0;
        }

        main {
            max-width: 850px;
            margin: 40px auto;
            background: white;
            padding: 30px;
            border-radius: 8px;
        }
    </style>
<link href="https://fonts.googleapis.com/css2?family=Inter:wght@400;500;600;700&family=Lora:wght@400;500;600;700&display=swap" rel="stylesheet">
<link rel="stylesheet" href="${pageContext.request.contextPath}/assets/gr6/navbar.css?v=1">
<link rel="stylesheet" href="${pageContext.request.contextPath}/assets/gr6/newsweb-theme.css?v=1">
</head>

<body>
<jsp:include page="/WEB-INF/views/partials/gr6-header.jsp" />

<main>
    <h1>Writer Dashboard</h1>

    <p>
        Welcome,
        <strong>
            <c:out value="${writerName}" />
        </strong>
    </p>

    <p>
        You have permission to access the
        editorial writing area.
    </p>

    

<p>
    Create editorial articles, manage your drafts,
    and submit finished articles for review.
</p>

<h2>Article Management</h2>

<p>
    <a href="${pageContext.request.contextPath}/writer/submit">
        + Write New Editorial Article
    </a>
</p>

<p>
    <a href="${pageContext.request.contextPath}/writer/articles">
        My Editorial Articles &amp; Drafts
    </a>
</p>

<h2>Public Content</h2>

<p>
    <a href="${pageContext.request.contextPath}/editorials">
        View Published Editorials
    </a>
</p>

<p>
    <a href="${pageContext.request.contextPath}/news">
        Back to News Homepage
    </a>
</p>

</main>

</body>
</html>
