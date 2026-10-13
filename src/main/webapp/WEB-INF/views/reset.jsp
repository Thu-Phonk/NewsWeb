
<%@ page contentType="text/html; charset=UTF-8"
         pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>

<!DOCTYPE html>
<html lang="en">
<head>
    <meta charset="UTF-8">
    <meta name="viewport"
          content="width=device-width, initial-scale=1">

    <title>Reset Password - NewsWeb</title>
</head>
<body>

    <h1>Reset Your Password</h1>

    <c:choose>

        <c:when test="${invalidToken}">
            <p style="color: red;">
                This password reset link is invalid,
                expired, or has already been used.
            </p>

            <a href="${pageContext.request.contextPath}/forgot">
                Request a New Reset Link
            </a>
        </c:when>

        <c:otherwise>

            <c:if test="${not empty error}">
                <p style="color: red;">
                    <c:out value="${error}" />
                </p>
            </c:if>

            <form action="${pageContext.request.contextPath}/reset"
                  method="post">

                <input type="hidden"
                       name="token"
                       value="<c:out value='${token}' />">

                <label for="password">
                    New Password
                </label>

                <input type="password"
                       id="password"
                       name="password"
                       minlength="8"
                       autocomplete="new-password"
                       required>

                <br><br>

                <label for="confirm_password">
                    Confirm New Password
                </label>

                <input type="password"
                       id="confirm_password"
                       name="confirm_password"
                       autocomplete="new-password"
                       required>

                <br><br>

                <button type="submit">
                    Reset Password
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
