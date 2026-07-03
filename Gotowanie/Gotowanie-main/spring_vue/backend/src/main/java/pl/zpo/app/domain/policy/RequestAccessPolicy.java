package pl.zpo.app.domain.policy;

import java.util.Objects;
import org.springframework.stereotype.Component;
import pl.zpo.app.domain.request.RequestEntity;
import pl.zpo.app.security.CurrentUser;

/**
 * Who may read, cancel or manage a given request. This is the server-side enforcement behind
 * "USER sees only their own requests, ADMIN sees everything, OPERATOR handles requests" — never
 * relying on the frontend to hide anything.
 */
@Component
public class RequestAccessPolicy implements DomainPolicy {

    /** ADMIN and OPERATOR may read any request; a USER may read only their own. */
    public boolean canRead(CurrentUser user, RequestEntity request) {
        if (user.isAdmin() || user.isOperator()) {
            return true;
        }
        return isOwner(user, request);
    }

    /** The owner may cancel their own cancellable request; ADMIN may cancel any cancellable one. */
    public boolean canCancel(CurrentUser user, RequestEntity request) {
        if (!request.getStatus().isCancellable()) {
            return false;
        }
        return user.isAdmin() || isOwner(user, request);
    }

    /** Managing = changing status / handling. Reserved for ADMIN and OPERATOR (not the owner). */
    public boolean canManage(CurrentUser user, RequestEntity request) {
        return user.isAdmin() || user.isOperator();
    }

    private boolean isOwner(CurrentUser user, RequestEntity request) {
        return Objects.equals(user.id(), request.getOwnerId());
    }
}
