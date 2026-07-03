package pl.zpo.app.domain.policy;

/**
 * Marker for domain access policies — small, pure classes that answer "may this user do this?".
 * They are deliberately free of Spring and persistence so they can be unit-tested directly
 * (see the policy tests). Implementations: {@link RequestAccessPolicy}, {@link AdminPolicy}.
 */
public interface DomainPolicy {

    default String name() {
        return getClass().getSimpleName();
    }
}
