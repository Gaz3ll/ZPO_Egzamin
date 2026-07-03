package pl.zpo.app.domain.policy;

import static org.assertj.core.api.Assertions.assertThat;
import static pl.zpo.app.support.TestFixtures.currentUser;
import static pl.zpo.app.support.TestFixtures.request;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import pl.zpo.app.domain.request.RequestEntity;
import pl.zpo.app.domain.request.RequestStatus;
import pl.zpo.app.security.CurrentUser;
import pl.zpo.app.users.Role;

/**
 * Security/authorization unit tests — the server-side enforcement that a USER only touches their
 * own requests while ADMIN/OPERATOR can see and manage everything. No Spring, no database.
 */
class RequestAccessPolicyTest {

    private final RequestAccessPolicy policy = new RequestAccessPolicy();

    private RequestEntity requestOwnedBy(long ownerId) {
        RequestEntity request = request(500L, 1L, null, null, null, RequestStatus.PENDING);
        request.setOwnerId(ownerId);
        return request;
    }

    // 11. USER can read their own request.
    @Test
    @DisplayName("11. USER can read own request")
    void userCanReadOwn() {
        CurrentUser user = currentUser(1L, Role.USER);
        assertThat(policy.canRead(user, requestOwnedBy(1L))).isTrue();
    }

    // 12. USER cannot read someone else's request.
    @Test
    @DisplayName("12. USER cannot read another user's request")
    void userCannotReadOthers() {
        CurrentUser user = currentUser(1L, Role.USER);
        assertThat(policy.canRead(user, requestOwnedBy(2L))).isFalse();
    }

    // 13. ADMIN can read any request.
    @Test
    @DisplayName("13. ADMIN can read any request")
    void adminCanReadAny() {
        CurrentUser admin = currentUser(9L, Role.ADMIN);
        assertThat(policy.canRead(admin, requestOwnedBy(2L))).isTrue();
    }

    // 14. USER cannot manage (handle) requests.
    @Test
    @DisplayName("14. USER cannot manage requests")
    void userCannotManage() {
        CurrentUser user = currentUser(1L, Role.USER);
        assertThat(policy.canManage(user, requestOwnedBy(1L))).isFalse();
    }

    // 15. ADMIN can manage (change status of) a request.
    @Test
    @DisplayName("15. ADMIN can manage (change status of) a request")
    void adminCanManage() {
        CurrentUser admin = currentUser(9L, Role.ADMIN);
        assertThat(policy.canManage(admin, requestOwnedBy(2L))).isTrue();
    }

    // --- Extra coverage ---

    @Test
    @DisplayName("OPERATOR can read any request")
    void operatorCanReadAny() {
        CurrentUser operator = currentUser(5L, Role.OPERATOR);
        assertThat(policy.canRead(operator, requestOwnedBy(2L))).isTrue();
    }

    @Test
    @DisplayName("OPERATOR can manage requests")
    void operatorCanManage() {
        CurrentUser operator = currentUser(5L, Role.OPERATOR);
        assertThat(policy.canManage(operator, requestOwnedBy(2L))).isTrue();
    }

    @Test
    @DisplayName("Owner can cancel a PENDING request; cannot cancel a COMPLETED one")
    void cancelRespectsStatus() {
        CurrentUser user = currentUser(1L, Role.USER);
        RequestEntity pending = requestOwnedBy(1L);
        assertThat(policy.canCancel(user, pending)).isTrue();

        RequestEntity completed = requestOwnedBy(1L);
        completed.setStatus(RequestStatus.COMPLETED);
        assertThat(policy.canCancel(user, completed)).isFalse();
    }

    @Test
    @DisplayName("USER cannot cancel another user's request")
    void userCannotCancelOthers() {
        CurrentUser user = currentUser(1L, Role.USER);
        assertThat(policy.canCancel(user, requestOwnedBy(2L))).isFalse();
    }
}
