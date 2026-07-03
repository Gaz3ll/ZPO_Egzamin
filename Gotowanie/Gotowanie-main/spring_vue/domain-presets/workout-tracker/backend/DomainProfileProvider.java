package pl.zpo.app.domain.config;

import java.util.List;
import org.springframework.stereotype.Component;

@Component
public class DomainProfileProvider {

    public DomainProfile getActiveProfile() {
        List<DomainFieldConfig> resourceFields = List.of(
                new DomainFieldConfig("exerciseName", "Nazwa ćwiczenia", FieldType.TEXT, true,
                        List.of(), "Nazwa ćwiczenia (np. Wyciskanie sztangi, Przysiad, Martwy ciąg)"),
                new DomainFieldConfig("muscleGroup", "Grupa mięśniowa", FieldType.SELECT, true,
                        List.of("CHEST", "BACK", "LEGS", "SHOULDERS", "ARMS", "CORE", "CARDIO"),
                        "CHEST-klatka piersiowa | BACK-plecy | LEGS-nogi | SHOULDERS-barki | ARMS-ręce | CORE-korpus | CARDIO-kardio"),
                new DomainFieldConfig("exerciseType", "Rodzaj ćwiczenia", FieldType.SELECT, true,
                        List.of("STRENGTH", "CARDIO", "STRETCHING", "CALISTHENICS"),
                        "STRENGTH-siłowe | CARDIO-cardio | STRETCHING-rozciąganie | CALISTHENICS-kalistenika"),
                new DomainFieldConfig("difficulty", "Poziom trudności", FieldType.SELECT, true,
                        List.of("BEGINNER", "INTERMEDIATE", "ADVANCED"),
                        "BEGINNER-początkujący | INTERMEDIATE-średniozaawansowany | ADVANCED-zaawansowany"),
                new DomainFieldConfig("equipment", "Sprzęt", FieldType.TEXT, false,
                        List.of(), "Sprzęt potrzebny do ćwiczenia")
        );

        List<DomainFieldConfig> requestFields = List.of(
                new DomainFieldConfig("sets", "Liczba serii", FieldType.NUMBER, true,
                        List.of(), "Liczba serii ćwiczenia (np. 3, 4, 5)"),
                new DomainFieldConfig("reps", "Liczba powtórzeń", FieldType.NUMBER, true,
                        List.of(), "Liczba powtórzeń w serii (np. 8, 10, 12)"),
                new DomainFieldConfig("weight", "Ciężar (kg)", FieldType.NUMBER, true,
                        List.of(), "Ciężar w kilogramach (np. 60, 80, 100)"),
                new DomainFieldConfig("durationMinutes", "Czas trwania (min)", FieldType.NUMBER, true,
                        List.of(), "Czas trwania ćwiczenia w minutach"),
                new DomainFieldConfig("workoutDate", "Data treningu", FieldType.DATE, true,
                        List.of(), "Data wykonania treningu (format RRRR-MM-DD)"),
                new DomainFieldConfig("notes", "Notatki", FieldType.TEXTAREA, false,
                        List.of(), "Dodatkowe uwagi (np. tempo, samopoczucie, progresja)")
        );

        return new DomainProfile(
                "Tracker treningów",
                "Ćwiczenie", "Ćwiczenia",
                "Trening", "Treningi",
                "KG",
                AlgorithmMode.VALUE_CALCULATION_ONLY,
                PricingUnit.FLAT,
                false,
                false,
                resourceFields,
                requestFields
        );
    }
}
