package pl.zpo.app.domain.policy;

import static org.assertj.core.api.Assertions.assertThat;
import static pl.zpo.app.support.TestFixtures.currentUser;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import pl.zpo.app.users.Role;

class AdminPolicyTest {

    private final AdminPolicy policy = new AdminPolicy();

    @Test
    @DisplayName("Only ADMIN may manage resources")
    void onlyAdminManagesResources() {
        assertThat(policy.canManageResources(currentUser(1L, Role.ADMIN))).isTrue();
        assertThat(policy.canManageResources(currentUser(2L, Role.OPERATOR))).isFalse();
        assertThat(policy.canManageResources(currentUser(3L, Role.USER))).isFalse();
    }

    @Test
    @DisplayName("ADMIN and OPERATOR may handle requests; USER may not")
    void adminAndOperatorHandleRequests() {
        assertThat(policy.canHandleRequests(currentUser(1L, Role.ADMIN))).isTrue();
        assertThat(policy.canHandleRequests(currentUser(2L, Role.OPERATOR))).isTrue();
        assertThat(policy.canHandleRequests(currentUser(3L, Role.USER))).isFalse();
    }

    @Test
    @DisplayName("Only ADMIN may manage users")
    void onlyAdminManagesUsers() {
        assertThat(policy.canManageUsers(currentUser(1L, Role.ADMIN))).isTrue();
        assertThat(policy.canManageUsers(currentUser(2L, Role.OPERATOR))).isFalse();
    }
}
