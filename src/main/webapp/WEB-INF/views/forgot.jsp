<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>
<!DOCTYPE html><html lang="en"><head><meta charset="UTF-8"><meta name="viewport" content="width=device-width,initial-scale=1">
<title>GR6 News · Forgot password</title>
<link href="https://fonts.googleapis.com/css2?family=Inter:wght@400;500;600;700&family=Lora:wght@600;700&display=swap" rel="stylesheet">
<link rel="stylesheet" href="${pageContext.request.contextPath}/assets/gr6/auth.css?v=1">
</head><body class="au-body"><main class="au-card"><section class="au-side">
<div class="au-top"><a href="${pageContext.request.contextPath}/news" class="au-logo">GR6 <b>NEWS</b></a><a class="au-back" href="${pageContext.request.contextPath}/news">← Home</a></div>
<div class="au-main">
<h1 class="au-title">Forgot your password?</h1>
<p class="au-lead">Enter your email to receive a password reset link.</p>
<c:choose><c:when test="${param.sent eq '1'}"><div class="au-alert au-alert--success" role="status">If an account exists with this email, a reset link will be sent shortly.</div></c:when>
<c:otherwise><form class="au-form" action="${pageContext.request.contextPath}/forgot" method="post">
 <input type="hidden" name="csrfToken" value="<c:out value='${sessionScope.forgotPasswordCsrfToken}' />">
 <div class="au-field"><label for="email">Email</label><input class="au-input" type="email" id="email" name="email" maxlength="254" placeholder="Email address" autocomplete="email" required></div>
 <button class="au-btn" type="submit">Send reset link →</button></form></c:otherwise></c:choose>
<p class="au-switch"><a href="${pageContext.request.contextPath}/login">← Back to sign in</a></p>
</div><div class="au-foot"><p class="au-copy">© GR6 News · Powered by Java Servlets &amp; MongoDB</p></div>
</section><aside class="au-visual" aria-hidden="true"><div class="au-glass"><span class="au-ring"></span><h2>Get back to reading.</h2><p>Securely recover your account and return to the conversation.</p></div></aside>
</main></body></html>
