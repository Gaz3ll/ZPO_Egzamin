package pl.zpo.app.domain.algorithm;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import org.springframework.stereotype.Component;
import pl.zpo.app.domain.resource.ResourceEntity;
import pl.zpo.app.domain.resource.ResourceStatus;

@Component
public class DefaultDomainAlgorithm implements DomainAlgorithm {

    @Override
    public DomainAlgorithmResult evaluate(DomainAlgorithmInput input) {
        AlgorithmBreakdownBuilder breakdown = new AlgorithmBreakdownBuilder("PLN");
        List<String> errors = new ArrayList<>();

        ResourceEntity resource = input.resource();
        if (resource == null) {
            return DomainAlgorithmResult.failure(List.of("Kategoria zgłoszenia jest wymagana"), breakdown.build());
        }
        if (resource.getStatus() != ResourceStatus.ACTIVE) {
            return DomainAlgorithmResult.failure(List.of("Kategoria nie jest aktywna"), breakdown.build());
        }
        breakdown.addRule("CATEGORY_CHECK: ok");

        String title = readString(input.requestMetadata(), "title");
        String description = readString(input.requestMetadata(), "description");
        String tenantName = readString(input.requestMetadata(), "tenantName");
        String contactPhone = readString(input.requestMetadata(), "contactPhone");
        String location = readString(input.requestMetadata(), "location");

        if (title == null) errors.add("Tytuł zgłoszenia jest wymagany");
        if (description == null) errors.add("Opis zgłoszenia jest wymagany");
        if (tenantName == null) errors.add("Imię i nazwisko jest wymagane");
        if (contactPhone == null) errors.add("Telefon kontaktowy jest wymagany");
        if (location == null) errors.add("Lokalizacja jest wymagana");

        String urgency = readString(input.requestMetadata(), "urgency");
        String defaultPriority = readString(resource.getMetadata(), "defaultPriority");

        breakdown.addRule("TITLE: " + (title != null ? title : "brak"));
        breakdown.addRule("URGENCY: " + (urgency != null ? urgency : "nie podano"));
        breakdown.addRule("DEFAULT_PRIORITY: " + (defaultPriority != null ? defaultPriority : "brak"));

        int priorityScore = switch (urgency != null ? urgency : "") {
            case "IMMEDIATE" -> 100;
            case "HIGH" -> 75;
            case "MEDIUM" -> 50;
            case "LOW" -> 25;
            default -> 30;
        };
        breakdown.addRule("PRIORITY_SCORE: " + priorityScore);

        if (!errors.isEmpty()) return DomainAlgorithmResult.failure(errors, breakdown.build());

        breakdown.addRule("ISSUE_REPORTED: OK");
        return DomainAlgorithmResult.success(BigDecimal.valueOf(priorityScore), resource.getId(), breakdown.build());
    }

    private String readString(Map<String, Object> m, String k) {
        if (m == null || m.get(k) == null) return null;
        String v = String.valueOf(m.get(k));
        return v.isBlank() ? null : v;
    }
}
