package pl.zpo.app.users;

/**
 * Application roles.
 *
 * <ul>
 *   <li>{@link #USER} — creates and views only their own requests.</li>
 *   <li>{@link #OPERATOR} — may view and handle (change status of) all requests, but
 *       may not manage resources or users.</li>
 *   <li>{@link #ADMIN} — full access: manages resources and handles all requests.</li>
 * </ul>
 */
public enum Role {
    USER,
    OPERATOR,
    ADMIN;

    /** Spring Security authority representation, e.g. {@code ROLE_ADMIN}. */
    public String authority() {
        return "ROLE_" + name();
    }
}
