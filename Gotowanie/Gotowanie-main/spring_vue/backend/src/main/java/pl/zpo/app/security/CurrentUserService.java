package pl.zpo.app.security;

import java.util.Optional;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import pl.zpo.app.exception.ForbiddenException;
import pl.zpo.app.exception.UnauthorizedException;
import pl.zpo.app.users.Role;

/**
 * Reads the authenticated caller from the {@code SecurityContext} and exposes convenient,
 * fail-fast accessors used by controllers and services. Keeps authorization checks out of
 * individual endpoints.
 */
@Service
public class CurrentUserService {

    /** @return the current user, or empty if the request is anonymous. */
    public Optional<CurrentUser> getCurrentUser() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || !auth.isAuthenticated() || !(auth.getPrincipal() instanceof SecurityUser principal)) {
            return Optional.empty();
        }
        return Optional.of(principal.toCurrentUser());
    }

    /** @return the current user or throw 401 if not authenticated. */
    public CurrentUser requireUser() {
        return getCurrentUser()
                .orElseThrow(() -> new UnauthorizedException("Authentication is required"));
    }

    /** Ensure the caller is authenticated and has exactly the given role, else 401/403. */
    public CurrentUser requireRole(Role role) {
        CurrentUser user = requireUser();
        if (user.role() != role) {
            throw new ForbiddenException("Requires role " + role);
        }
        return user;
    }

    /** Ensure the caller is authenticated and has any of the given roles, else 401/403. */
    public CurrentUser requireAnyRole(Role... roles) {
        CurrentUser user = requireUser();
        for (Role role : roles) {
            if (user.role() == role) {
                return user;
            }
        }
        throw new ForbiddenException("Insufficient role");
    }
}
