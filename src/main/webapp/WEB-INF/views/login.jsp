
<%@ page contentType="text/html; charset=UTF-8"
         pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>

<!DOCTYPE html>
<html lang="en">
<head>
    <meta charset="UTF-8">
    <meta name="viewport"
          content="width=device-width, initial-scale=1">

    <title>NewsWeb Login</title>
</head>
<body>

    <h1>NewsWeb Login</h1>

    <c:choose>

        <c:when test="${not empty sessionScope.userId}">

            <h2>Login successful!</h2>

            <p>
                Welcome,
                <c:out value="${sessionScope.username}" />!
            </p>

            <p>
                Role:
                <c:out value="${sessionScope.role}" />
            </p>

            <a href="${pageContext.request.contextPath}/news">
                View news
            </a>

            <form action="${pageContext.request.contextPath}/logout"
                  method="post">

                <button type="submit">Logout</button>

            </form>

        </c:when>

        <c:otherwise>

            <c:if test="${not empty error}">
                <p style="color: red;">
                    <c:out value="${error}" />
                </p>
            </c:if>

            <form action="${pageContext.request.contextPath}/login"
                  method="post">

                <label for="username">
                    Username or Email
                </label>

                <input type="text"
                       id="username"
                       name="username"
                       required
                       autocomplete="username">

                <br><br>

                <label for="password">Password</label>

                <input type="password"
                       id="password"
                       name="password"
                       required
                       autocomplete="current-password">

                <br><br>

                <button type="submit">Login</button>

            </form>
	
<p>
    Don't have an account?
    <a href="${pageContext.request.contextPath}/register">
        Create an account
    </a>
</p>
<p>
    <a href="${pageContext.request.contextPath}/forgot">
        Forgot your password?
    </a>
</p>
<c:if test="${param.registered eq '1'}">
    <p style="color: green;">
        Registration complete! Please log in.
    </p>
<c:if test="${param.reset eq 'success'}">
    <p style="color: green;">
        Your password has been reset successfully.
        Please log in with your new password.
    </p>
</c:if>

</c:if>

        </c:otherwise>

    </c:choose>

</body>
</html>
