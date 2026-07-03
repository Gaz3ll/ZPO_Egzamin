package pl.zpo.app.security;

import pl.zpo.app.users.Role;

/**
 * Minimal, immutable snapshot of the authenticated caller. Passed to access policies so
 * they can be unit-tested without Spring Security or a database.
 */
public record CurrentUser(Long id, String email, Role role) {

    public boolean isAdmin() {
        return role == Role.ADMIN;
    }

    public boolean isOperator() {
        return role == Role.OPERATOR;
    }

    public boolean hasRole(Role expected) {
        return role == expected;
    }
}
