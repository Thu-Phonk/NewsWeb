<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<!DOCTYPE html><html lang="en"><head><meta charset="UTF-8"><meta name="viewport" content="width=device-width,initial-scale=1">
<title>GR6 News · Reset password</title>
<link href="https://fonts.googleapis.com/css2?family=Inter:wght@400;500;600;700&family=Lora:wght@600;700&display=swap" rel="stylesheet">
<link rel="stylesheet" href="${pageContext.request.contextPath}/assets/gr6/auth.css?v=1">
</head><body class="au-body"><main class="au-card"><section class="au-side">
<div class="au-top"><a href="${pageContext.request.contextPath}/news" class="au-logo">GR6 <b>NEWS</b></a><a class="au-back" href="${pageContext.request.contextPath}/news">← Home</a></div>
<div class="au-main">
<h1 class="au-title">Reset your password</h1>
<c:choose><c:when test="${invalidToken}"><div class="au-alert au-alert--error" role="alert">This password reset link is invalid, expired, or already used.</div>
<p class="au-switch"><a href="${pageContext.request.contextPath}/forgot">Request another link</a></p></c:when>
<c:otherwise>
 <p class="au-lead">Choose a new password for your account.</p>
 <c:if test="${not empty error}"><div class="au-alert au-alert--error" role="alert"><c:out value="${error}" /></div></c:if>
 <form class="au-form" action="${pageContext.request.contextPath}/reset" method="post">
  <input type="hidden" name="token" value="<c:out value='${token}' />">
  <div class="au-field"><label for="password">New password</label><input class="au-input" type="password" name="password" id="password" minlength="8" placeholder="New password" autocomplete="new-password" required></div>
  <div class="au-field"><label for="confirm_password">Confirm password</label><input class="au-input" type="password" name="confirm_password" id="confirm_password" minlength="8" placeholder="Confirm password" autocomplete="new-password" required></div>
  <button class="au-btn" type="submit">Reset password →</button>
 </form>
</c:otherwise></c:choose>
<p class="au-switch"><a href="${pageContext.request.contextPath}/login">← Back to sign in</a></p>
</div><div class="au-foot"><p class="au-copy">© GR6 News · Powered by Java Servlets &amp; MongoDB</p></div>
</section><aside class="au-visual" aria-hidden="true"><div class="au-glass"><span class="au-ring"></span><h2>A fresh start.</h2><p>Your security matters. Protect your account with a strong, unique password.</p></div></aside>
</main></body></html>
