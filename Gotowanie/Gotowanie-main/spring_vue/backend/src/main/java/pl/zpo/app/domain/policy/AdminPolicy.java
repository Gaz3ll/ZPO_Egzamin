package pl.zpo.app.domain.policy;

import org.springframework.stereotype.Component;
import pl.zpo.app.security.CurrentUser;

/**
 * Coarse-grained administrative capabilities. Mirrors the URL rules in {@code SecurityConfig}
 * (defense in depth) and is used by admin services to double-check the caller.
 */
@Component
public class AdminPolicy implements DomainPolicy {

    /** Only ADMIN manages resources (create/update). */
    public boolean canManageResources(CurrentUser user) {
        return user.isAdmin();
    }

    /** ADMIN and OPERATOR may handle (view all / change status of) requests. */
    public boolean canHandleRequests(CurrentUser user) {
        return user.isAdmin() || user.isOperator();
    }

    /** Only ADMIN manages users. */
    public boolean canManageUsers(CurrentUser user) {
        return user.isAdmin();
    }
}
