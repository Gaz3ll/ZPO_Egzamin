package pl.zpo.app.domain.config;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import java.util.Arrays;
import java.util.List;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import pl.zpo.app.common.ApiResponse;
import pl.zpo.app.domain.request.RequestStatus;
import pl.zpo.app.domain.resource.ResourceStatus;
import pl.zpo.app.users.Role;

/**
 * Public, generic endpoint exposing the active {@link DomainProfile} and enum vocabularies so a
 * client can render the correct labels/fields and validate against the same definitions the
 * backend uses. Not domain-specific — it returns whatever {@code DomainProfileProvider} defines.
 */
@RestController
@RequestMapping("/api/config")
@Tag(name = "Config", description = "Active domain profile and enum vocabularies")
public class ConfigController {

    private final DomainProfile profile;

    public ConfigController(DomainProfile profile) {
        this.profile = profile;
    }

    @GetMapping
    @Operation(summary = "Get the active domain profile (labels, fields, algorithm mode) and enums")
    public ApiResponse<DomainConfigView> getConfig() {
        return ApiResponse.ok(new DomainConfigView(
                profile,
                names(Role.values()),
                names(ResourceStatus.values()),
                names(RequestStatus.values())));
    }

    private List<String> names(Enum<?>[] values) {
        return Arrays.stream(values).map(Enum::name).toList();
    }
}
