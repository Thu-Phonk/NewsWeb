
<%@ page contentType="text/html; charset=UTF-8"
         pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>

<!DOCTYPE html>
<html lang="en">
<head>
    <meta charset="UTF-8">
    <meta name="viewport"
          content="width=device-width, initial-scale=1">

    <title>Forgot Password - NewsWeb</title>
</head>
<body>

    <h1>Forgot Password</h1>

    <c:choose>
        <c:when test="${param.sent eq '1'}">
            <p>
                If an account exists with that email address,
                a password reset link will be sent shortly.
            </p>
        </c:when>

        <c:otherwise>
            <p>
                Enter your registered email address
                to request a password reset.
            </p>

            <form action="${pageContext.request.contextPath}/forgot"
                  method="post">

                <label for="email">Email address</label>

                <input type="email"
                       id="email"
                       name="email"
                       maxlength="254"
                       required>

                <button type="submit">
                    Send Reset Link
                </button>

            </form>
        </c:otherwise>
    </c:choose>

    <p>
        <a href="${pageContext.request.contextPath}/login">
            Back to Login
        </a>
    </p>

</body>
</html>
