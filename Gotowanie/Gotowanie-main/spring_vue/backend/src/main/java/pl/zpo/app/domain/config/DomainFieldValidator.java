package pl.zpo.app.domain.config;

import java.math.BigDecimal;
import java.time.format.DateTimeParseException;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import org.springframework.stereotype.Component;
import pl.zpo.app.exception.ValidationException;

/**
 * Validates a resource's / request's dynamic {@code metadata} map against the field definitions
 * from the active {@link DomainProfile}. This is how domain-specific validation changes when you
 * re-target the system: edit the fields in {@link DomainProfileProvider}, no code change here.
 * Throws {@link ValidationException} (HTTP 400) with per-field messages.
 */
@Component
public class DomainFieldValidator {

    public void validate(Map<String, Object> metadata, List<DomainFieldConfig> fields, String context) {
        Map<String, Object> data = metadata == null ? Map.of() : metadata;
        Map<String, String> errors = new LinkedHashMap<>();

        for (DomainFieldConfig field : fields) {
            Object value = data.get(field.key());
            boolean empty = value == null || (value instanceof String s && s.isBlank());

            if (empty) {
                if (field.required()) {
                    errors.put(field.key(), field.label() + " jest wymagane");
                }
                continue;
            }
            validateType(field, value, errors);
        }

        if (!errors.isEmpty()) {
            throw new ValidationException("Nieprawidłowe pola: " + context, errors);
        }
    }

    private void validateType(DomainFieldConfig field, Object value, Map<String, String> errors) {
        switch (field.type()) {
            case NUMBER -> {
                if (!isNumeric(value)) {
                    errors.put(field.key(), field.label() + " musi być liczbą");
                }
            }
            case SELECT -> {
                if (!field.options().contains(String.valueOf(value))) {
                    errors.put(field.key(), field.label() + " musi być jedną z: " + field.options());
                }
            }
            case BOOLEAN -> {
                if (!(value instanceof Boolean) && !"true".equals(value) && !"false".equals(value)) {
                    errors.put(field.key(), field.label() + " musi być wartością logiczną");
                }
            }
            case DATE -> {
                if (!isIsoDate(value)) {
                    errors.put(field.key(), field.label() + " musi być datą w formacie ISO (RRRR-MM-DD)");
                }
            }
            case TEXT, TEXTAREA -> {
                // any non-empty value is acceptable
            }
        }
    }

    private boolean isNumeric(Object value) {
        if (value instanceof Number) {
            return true;
        }
        try {
            new BigDecimal(String.valueOf(value));
            return true;
        } catch (NumberFormatException ex) {
            return false;
        }
    }

    private boolean isIsoDate(Object value) {
        try {
            java.time.LocalDate.parse(String.valueOf(value));
            return true;
        } catch (DateTimeParseException ex) {
            return false;
        }
    }
}
