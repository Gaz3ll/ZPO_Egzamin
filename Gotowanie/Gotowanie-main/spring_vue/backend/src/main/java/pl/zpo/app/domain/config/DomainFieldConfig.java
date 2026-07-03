package pl.zpo.app.domain.config;

import java.util.List;

/**
 * Declarative definition of one domain-specific attribute stored in a resource's or request's
 * {@code metadata} JSONB. The same list drives dynamic form rendering on the frontend and
 * server-side validation (see {@code DomainFieldValidator}). Change these in
 * {@code DomainProfileProvider} to re-shape the domain without touching entities or endpoints.
 *
 * @param key      metadata key, e.g. "brand", "lockerSize", "specialization"
 * @param label    human label shown in the UI
 * @param type     input/validation type
 * @param required whether the field must be present and non-empty
 * @param options  allowed values for {@link FieldType#SELECT} (otherwise empty)
 * @param helpText optional helper text
 */
public record DomainFieldConfig(
        String key,
        String label,
        FieldType type,
        boolean required,
        List<String> options,
        String helpText
) {

    public DomainFieldConfig {
        options = options == null ? List.of() : List.copyOf(options);
    }

    public static DomainFieldConfig text(String key, String label, boolean required) {
        return new DomainFieldConfig(key, label, FieldType.TEXT, required, List.of(), null);
    }

    public static DomainFieldConfig number(String key, String label, boolean required) {
        return new DomainFieldConfig(key, label, FieldType.NUMBER, required, List.of(), null);
    }

    public static DomainFieldConfig select(String key, String label, boolean required, List<String> options) {
        return new DomainFieldConfig(key, label, FieldType.SELECT, required, options, null);
    }
}
