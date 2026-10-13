
package com.example.news.util;

import com.example.news.user.UserCollection;
import com.mongodb.client.model.Filters;
import com.mongodb.client.model.Projections;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;

import org.bson.Document;
import org.bson.types.ObjectId;

import java.util.Set;

public final class RoleUtil {

    private RoleUtil() {
    }

    public static Document getCurrentUser(
            HttpServletRequest request) {

        HttpSession session = request.getSession(false);

        if (session == null) {
            return null;
        }

        Object value = session.getAttribute("userId");

        if (!(value instanceof String userId)
                || !ObjectId.isValid(userId)) {
            return null;
        }

        return UserCollection.getUsers()
                .find(Filters.eq(
                        "_id", new ObjectId(userId)
                ))
                .projection(Projections.include(
                        "_id", "username", "role"
                ))
                .first();
    }

    public static boolean hasRole(
            Document user, String... allowedRoles) {

        if (user == null) {
            return false;
        }

        String role = user.getString("role");

        if (role == null) {
            return false;
        }

        return Set.of(allowedRoles).contains(role);
    }
}
