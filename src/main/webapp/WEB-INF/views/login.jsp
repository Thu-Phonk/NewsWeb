<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<!DOCTYPE html><html lang="en"><head><meta charset="UTF-8"><meta name="viewport" content="width=device-width,initial-scale=1">
<title>GR6 News · Sign in</title>
<link href="https://fonts.googleapis.com/css2?family=Inter:wght@400;500;600;700&family=Lora:wght@600;700&display=swap" rel="stylesheet">
<link rel="stylesheet" href="${pageContext.request.contextPath}/assets/gr6/auth.css?v=1">
</head><body class="au-body"><main class="au-card"><section class="au-side">
<div class="au-top"><a href="${pageContext.request.contextPath}/news" class="au-logo">GR6 <b>NEWS</b></a><a class="au-back" href="${pageContext.request.contextPath}/news">← Home</a></div>
<div class="au-main">
<c:choose>
<c:when test="${not empty sessionScope.userId}">
  <h1 class="au-title">Welcome back!</h1>
  <p class="au-lead">Signed in as <strong><c:out value="${sessionScope.username}" /></strong>.</p>
  <a class="au-btn" style="text-decoration:none" href="${pageContext.request.contextPath}/news">Continue to GR6 News →</a>
  <form class="au-form" action="${pageContext.request.contextPath}/logout" method="post" style="margin-top:18px">
    <input type="hidden" name="csrfToken" value="<c:out value='${sessionScope.logoutCsrfToken}' />">
    <button type="submit" class="au-btn">Sign out</button>
  </form>
</c:when>
<c:otherwise>
<h1 class="au-title">Welcome back!</h1>
<p class="au-lead">Sign in to discuss articles, save favorites, and access your workspace.</p>
<c:if test="${not empty error}"><div class="au-alert au-alert--error" role="alert"><c:out value="${error}" /></div></c:if>
<c:if test="${param.registered eq '1'}"><div class="au-alert au-alert--success">Account created. Please sign in.</div></c:if>
<c:if test="${param.reset eq 'success'}"><div class="au-alert au-alert--success">Password updated. Please sign in.</div></c:if>
<form class="au-form" action="${pageContext.request.contextPath}/login" method="post">
 <input type="hidden" name="csrfToken" value="<c:out value='${sessionScope.loginCsrfToken}' />">
 <div class="au-field"><label for="username">Username or email</label><input class="au-input" id="username" name="username" type="text" placeholder="Username or email" autocomplete="username" maxlength="254" required></div>
 <div class="au-field au-field--pass"><label for="password">Password</label><input class="au-input" id="password" name="password" type="password" placeholder="Password" autocomplete="current-password" required></div>
 <div class="au-link-row"><a class="au-link" href="${pageContext.request.contextPath}/forgot">Forgot password?</a></div>
 <button class="au-btn" type="submit">Sign in →</button>
</form>
<p class="au-switch">New here? <a href="${pageContext.request.contextPath}/register">Create an account</a></p>
</c:otherwise>
</c:choose>
</div><div class="au-foot"><p class="au-copy">© GR6 News · Powered by Java Servlets &amp; MongoDB</p></div>
</section><aside class="au-visual" aria-hidden="true"><div class="au-glass"><span class="au-ring"></span><h2>The world, in perspective.</h2><p>Trusted stories, thoughtful editorial, and a place for your voice.</p></div></aside>
</main></body></html>
