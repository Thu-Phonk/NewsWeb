<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<!DOCTYPE html><html lang="en"><head><meta charset="UTF-8"><meta name="viewport" content="width=device-width,initial-scale=1">
<title>GR6 News · Register</title>
<link href="https://fonts.googleapis.com/css2?family=Inter:wght@400;500;600;700&family=Lora:wght@600;700&display=swap" rel="stylesheet">
<link rel="stylesheet" href="${pageContext.request.contextPath}/assets/gr6/auth.css?v=1">
</head><body class="au-body"><main class="au-card"><section class="au-side">
<div class="au-top"><a href="${pageContext.request.contextPath}/news" class="au-logo">GR6 <b>NEWS</b></a><a class="au-back" href="${pageContext.request.contextPath}/news">← Home</a></div>
<div class="au-main">
<h1 class="au-title">Join GR6 News</h1><p class="au-lead">Create your account and join the conversation.</p>
<c:if test="${not empty error}"><div class="au-alert au-alert--error" role="alert"><c:out value="${error}" /></div></c:if>
<form class="au-form" action="${pageContext.request.contextPath}/register" method="post">
 <input type="hidden" name="csrfToken" value="<c:out value='${sessionScope.registerCsrfToken}' />">
 <div class="au-field"><label for="fullName">Full name</label><input class="au-input" id="fullName" name="fullName" type="text" placeholder="Full name" maxlength="100" autocomplete="name" required></div>
 <div class="au-row">
  <div class="au-field"><label for="username">Username</label><input class="au-input" id="username" name="username" placeholder="Username" minlength="3" maxlength="30" autocomplete="username" required></div>
  <div class="au-field"><label for="email">Email</label><input class="au-input" id="email" name="email" type="email" placeholder="Email address" maxlength="254" autocomplete="email" required></div>
 </div>
 <div class="au-field au-field--pass"><label for="password">Password</label><input class="au-input" id="password" name="password" type="password" placeholder="Password (at least 8 characters)" minlength="8" autocomplete="new-password" required></div>
 <div class="au-field au-field--pass"><label for="confirmPassword">Confirm password</label><input class="au-input" id="confirmPassword" name="confirmPassword" type="password" placeholder="Confirm password" minlength="8" autocomplete="new-password" required></div>
 <button class="au-btn" type="submit">Create account →</button>
</form><p class="au-switch">Already have an account? <a href="${pageContext.request.contextPath}/login">Sign in</a></p>
</div><div class="au-foot"><p class="au-copy">© GR6 News · Powered by Java Servlets &amp; MongoDB</p></div>
</section><aside class="au-visual" aria-hidden="true"><div class="au-glass"><span class="au-ring"></span><h2>Every day, a fresh point of view.</h2><p>Discover stories written by our community, and contribute your own.</p></div></aside>
</main></body></html>
