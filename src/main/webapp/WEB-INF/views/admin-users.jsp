
<%@ page contentType="text/html; charset=UTF-8"
         pageEncoding="UTF-8" %>
<%@ taglib prefix="c" uri="jakarta.tags.core" %>

<!DOCTYPE html>
<html lang="en">
<head>
    <meta charset="UTF-8">
    <meta name="viewport"
          content="width=device-width, initial-scale=1">

    <title>User Management - NewsWeb</title>

    <style>
        body {
            font-family: Arial, sans-serif;
            background: #f4f4f4;
            margin: 0;
        }

        header {
            background: #222;
            color: white;
            padding: 25px;
            text-align: center;
        }

        header a {
            color: white;
            margin: 10px;
        }

        main {
            max-width: 1100px;
            margin: 30px auto;
            background: white;
            padding: 25px;
            border-radius: 8px;
        }

        table {
            width: 100%;
            border-collapse: collapse;
        }

        th, td {
            border-bottom: 1px solid #ddd;
            padding: 12px;
            text-align: left;
        }

        th {
            background: #eee;
        }

        select, button {
            padding: 8px;
        }

        button {
            background: #222;
            color: white;
            border: none;
            border-radius: 4px;
            cursor: pointer;
        }

        .success {
            color: green;
        }

        .note {
            color: #666;
            font-size: 14px;
        }
    </style>
</head>

<body>

<header>
    <h1>Administrator Dashboard</h1>
    <p>NewsWeb User Management</p>

    <nav>
        <a href="${pageContext.request.contextPath}/news">
            News
        </a>

        <a href="${pageContext.request.contextPath}/editor">
            Editorial Reviews
        </a>

        <a href="${pageContext.request.contextPath}/editor/blogs">
            Blog Moderation
        </a>
    </nav>
</header>

<main>

    <h2>Manage User Roles</h2>

    <p class="note">
        Only administrators may change account roles.
    </p>

    <c:if test="${param.updated eq '1'}">
        <p class="success">
            User role updated successfully!
        </p>
    </c:if>

    <table>
        <thead>
            <tr>
                <th>Username</th>
                <th>Full Name</th>
                <th>Email</th>
                <th>Current Role</th>
                <th>Change Role</th>
            </tr>
        </thead>

        <tbody>

            <c:forEach var="user" items="${users}">

                <tr>
                    <td>
                        <c:out value="${user.username}" />
                    </td>

                    <td>
                        <c:out value="${user.full_name}" />
                    </td>

                    <td>
                        <c:out value="${user.email}" />
                    </td>

                    <td>
                        <strong>
                            <c:out value="${user.role}" />
                        </strong>
                    </td>

                    <td>
                        <c:choose>
                            <c:when test="${user.username eq adminUsername}">
                                Your account — cannot change own role
                            </c:when>

                            <c:otherwise>

                                <form
                                    action="${pageContext.request.contextPath}/admin/users"
                                    method="post">

                                    <input type="hidden"
                                           name="userId"
                                           value="<c:out value='${user._id}' />">

                                    <input type="hidden"
                                           name="csrfToken"
                                           value="<c:out value='${sessionScope.adminCsrfToken}' />">

                                    <select name="role">

                                        <option value="guest"
                                            ${user.role eq 'guest' ? 'selected' : ''}>
                                            Guest
                                        </option>

                                        <option value="subscriber"
                                            ${user.role eq 'subscriber' ? 'selected' : ''}>
                                            Subscriber
                                        </option>

                                        <option value="writer"
                                            ${user.role eq 'writer' ? 'selected' : ''}>
                                            Writer
                                        </option>

                                        <option value="editor"
                                            ${user.role eq 'editor' ? 'selected' : ''}>
                                            Editor
                                        </option>

                                        <option value="administrator"
                                            ${user.role eq 'administrator' ? 'selected' : ''}>
                                            Administrator
                                        </option>

                                    </select>

                                    <button type="submit">
                                        Save Role
                                    </button>

                                </form>

                            </c:otherwise>
                        </c:choose>
                    </td>
                </tr>

            </c:forEach>

        </tbody>
    </table>

    <p class="note">
        Displaying up to 100 user accounts.
        Pagination can be added later.
    </p>

</main>
</body>
</html>
