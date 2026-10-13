
<%@ page contentType="text/html; charset=UTF-8"
         pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>

<!DOCTYPE html>
<html lang="en">
<head>
    <meta charset="UTF-8">
    <meta name="viewport"
          content="width=device-width, initial-scale=1">
    <title>Register - NewsWeb</title>
</head>
<body>

    <h1>Create Your NewsWeb Account</h1>

    <c:if test="${not empty error}">
        <p style="color: red;">
            <c:out value="${error}" />
        </p>
    </c:if>

    <form action="${pageContext.request.contextPath}/register"
          method="post">

        <input type="hidden"
               name="csrfToken"
               value="${sessionScope.registerCsrfToken}">

        <label for="username">Username</label>
        <input type="text"
               id="username"
               name="username"
               minlength="3"
               maxlength="30"
               required>

        <br><br>

        <label for="fullName">Full Name</label>
        <input type="text"
               id="fullName"
               name="fullName"
               maxlength="100"
               required>

        <br><br>

        <label for="email">Email</label>
        <input type="email"
               id="email"
               name="email"
               required>

        <br><br>

        <label for="password">Password</label>
        <input type="password"
               id="password"
               name="password"
               minlength="8"
               required>

        <br><br>

        <label for="confirmPassword">Confirm Password</label>
        <input type="password"
               id="confirmPassword"
               name="confirmPassword"
               required>

        <br><br>

        <button type="submit">Register</button>

    </form>

    <p>
        Already have an account?
        <a href="${pageContext.request.contextPath}/login">
            Login here
        </a>
    </p>

</body>
</html>
