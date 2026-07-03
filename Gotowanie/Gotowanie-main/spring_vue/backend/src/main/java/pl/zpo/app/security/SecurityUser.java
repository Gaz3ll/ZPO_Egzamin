package pl.zpo.app.security;

import java.util.Collection;
import java.util.List;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import pl.zpo.app.users.Role;
import pl.zpo.app.users.UserEntity;

/**
 * Spring Security principal. Wraps the persisted user id, email and role, and is stored in
 * the {@code SecurityContext} by {@link JwtAuthenticationFilter}. The password hash is kept
 * only so this can also serve as {@link UserDetails} during username/password login.
 */
public class SecurityUser implements UserDetails {

    private final Long id;
    private final String email;
    private final String passwordHash;
    private final Role role;

    public SecurityUser(Long id, String email, String passwordHash, Role role) {
        this.id = id;
        this.email = email;
        this.passwordHash = passwordHash;
        this.role = role;
    }

    public static SecurityUser from(UserEntity user) {
        return new SecurityUser(user.getId(), user.getEmail(), user.getPasswordHash(), user.getRole());
    }

    public Long getId() {
        return id;
    }

    public Role getRole() {
        return role;
    }

    public CurrentUser toCurrentUser() {
        return new CurrentUser(id, email, role);
    }

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return List.of(new SimpleGrantedAuthority(role.authority()));
    }

    @Override
    public String getPassword() {
        return passwordHash;
    }

    @Override
    public String getUsername() {
        return email;
    }

    @Override
    public boolean isAccountNonExpired() {
        return true;
    }

    @Override
    public boolean isAccountNonLocked() {
        return true;
    }

    @Override
    public boolean isCredentialsNonExpired() {
        return true;
    }

    @Override
    public boolean isEnabled() {
        return true;
    }
}
