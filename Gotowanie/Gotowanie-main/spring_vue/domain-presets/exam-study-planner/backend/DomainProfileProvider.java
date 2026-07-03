package pl.zpo.app.domain.config;

import java.util.List;
import org.springframework.stereotype.Component;

/**
 * Exam study planner profile. Resource = an exam with a date and a material scope, Request = a
 * generated study plan / study session. baseValue = estimated exam difficulty (multiplier, e.g.
 * 1.0 easy … 2.0 very hard), capacityValue = available study blocks per day.
 */
@Component
public class DomainProfileProvider {

    public DomainProfile getActiveProfile() {
        List<DomainFieldConfig> resourceFields = List.of(
                new DomainFieldConfig("examDate", "Data egzaminu", FieldType.DATE, true,
                        List.of(), "Dzień egzaminu (RRRR-MM-DD)"),
                new DomainFieldConfig("subject", "Przedmiot", FieldType.TEXT, true,
                        List.of(), "Np. Matematyka dyskretna"),
                new DomainFieldConfig("difficulty", "Trudność", FieldType.SELECT, true,
                        List.of("EASY", "MEDIUM", "HARD"), "Wpływa na szacowany czas nauki"),
                new DomainFieldConfig("materialCount", "Ilość materiału", FieldType.NUMBER, true,
                        List.of(), "Liczba tematów / rozdziałów / stron"),
                new DomainFieldConfig("materialUnit", "Jednostka materiału", FieldType.SELECT, true,
                        List.of("TOPICS", "CHAPTERS", "PAGES"), "Czym liczony jest materiał"),
                new DomainFieldConfig("topics", "Lista tematów", FieldType.TEXTAREA, false,
                        List.of(), "Tematy/rozdziały do przerobienia (po przecinku)"),
                new DomainFieldConfig("dailyStudyLimitMinutes", "Limit nauki dziennie (min)", FieldType.NUMBER, false,
                        List.of(), "Maksymalny czas nauki na dzień")
        );

        List<DomainFieldConfig> requestFields = List.of(
                new DomainFieldConfig("studyDate", "Data nauki", FieldType.DATE, true,
                        List.of(), "Kiedy odbywa się sesja"),
                new DomainFieldConfig("studyMinutes", "Czas nauki (min)", FieldType.NUMBER, true,
                        List.of(), "Ile minut poświęcono na naukę"),
                new DomainFieldConfig("materialDone", "Przerobiony materiał", FieldType.NUMBER, true,
                        List.of(), "Ilość przerobionego materiału"),
                new DomainFieldConfig("selectedTopics", "Wybrane tematy", FieldType.TEXTAREA, false,
                        List.of(), "Podzbiór materiału do tego planu"),
                new DomainFieldConfig("priority", "Priorytet", FieldType.SELECT, false,
                        List.of("LOW", "NORMAL", "HIGH"), "Priorytet planu"),
                new DomainFieldConfig("isRevision", "Sesja powtórkowa", FieldType.BOOLEAN, false,
                        List.of(), "Czy to plan powtórki"),
                new DomainFieldConfig("completed", "Zrealizowany", FieldType.BOOLEAN, false,
                        List.of(), "Czy plan został zrealizowany"),
                new DomainFieldConfig("notes", "Notatki", FieldType.TEXTAREA, false,
                        List.of(), "Uwagi do planu nauki")
        );

        return new DomainProfile(
                "Planer Nauki",
                "Egzamin", "Egzaminy",
                "Plan nauki", "Plany nauki",
                "MIN",
                AlgorithmMode.VALUE_CALCULATION_ONLY,
                PricingUnit.FLAT,
                /* requiresTimeWindow */ false,
                /* requiresQuantity  */ false,
                resourceFields,
                requestFields
        );
    }
}
