package pl.zpo.app.domain.config;

import java.util.List;

/**
 * What {@code GET /api/config} returns: the active domain profile plus the enum vocabularies the
 * frontend needs (roles, statuses). Proves the backend is the source of truth for the domain shape.
 */
public record DomainConfigView(
        DomainProfile profile,
        List<String> roles,
        List<String> resourceStatuses,
        List<String> requestStatuses
) {
}
