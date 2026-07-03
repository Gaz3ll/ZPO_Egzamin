package pl.zpo.app.config;

import java.util.List;
import java.util.Map;
import org.slf4j.Logger;
import pl.zpo.app.domain.config.DomainFieldConfig;
import pl.zpo.app.domain.config.DomainProfile;
import pl.zpo.app.domain.request.RequestRepository;
import pl.zpo.app.domain.resource.ResourceEntity;
import pl.zpo.app.domain.resource.ResourceRepository;

/**
 * Shared seed helper for the generic app and copied domain presets.
 *
 * <p>When a preset is applied over an already-used database, stale resources from the previous
 * preset would otherwise remain because demo users already exist. This helper detects whether the
 * existing resources match the active {@link DomainProfile}. If not, it resets only domain demo
 * data ({@code requests} and {@code resources}); users are kept and refreshed separately.</p>
 */
public final class PresetSeedSupport {

    private PresetSeedSupport() {
    }

    public static boolean prepareDomainSeed(ResourceRepository resourceRepository,
                                            RequestRepository requestRepository,
                                            DomainProfile profile,
                                            Logger log) {
        List<ResourceEntity> resources = resourceRepository.findAll();
        long requestCount = requestRepository.count();

        if (!resources.isEmpty() && requestCount > 0 && resourcesMatchProfile(resources, profile)) {
            log.info("Seed data for domain '{}' already matches the active profile; skipping.", profile.domainName());
            return false;
        }

        if (!resources.isEmpty() || requestCount > 0) {
            log.info("Resetting resources and requests before seeding domain '{}'...", profile.domainName());
            requestRepository.deleteAllInBatch();
            resourceRepository.deleteAllInBatch();
        }

        return true;
    }

    private static boolean resourcesMatchProfile(List<ResourceEntity> resources, DomainProfile profile) {
        List<String> expectedKeys = expectedResourceKeys(profile);
        if (expectedKeys.isEmpty()) {
            return !resources.isEmpty();
        }

        for (ResourceEntity resource : resources) {
            Map<String, Object> metadata = resource.getMetadata();
            if (metadata != null && metadata.keySet().containsAll(expectedKeys)) {
                return true;
            }
        }
        return false;
    }

    private static List<String> expectedResourceKeys(DomainProfile profile) {
        List<String> requiredKeys = profile.resourceFields().stream()
                .filter(DomainFieldConfig::required)
                .map(DomainFieldConfig::key)
                .toList();
        if (!requiredKeys.isEmpty()) {
            return requiredKeys;
        }
        return profile.resourceFields().stream()
                .map(DomainFieldConfig::key)
                .toList();
    }
}
