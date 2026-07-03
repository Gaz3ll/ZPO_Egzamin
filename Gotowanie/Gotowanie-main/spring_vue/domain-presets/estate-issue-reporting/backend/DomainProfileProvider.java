package pl.zpo.app.domain.config;

import java.util.List;
import org.springframework.stereotype.Component;

@Component
public class DomainProfileProvider {

    public DomainProfile getActiveProfile() {
        List<DomainFieldConfig> resourceFields = List.of(
                new DomainFieldConfig("categoryName", "Nazwa kategorii", FieldType.TEXT, true,
                        List.of(), "Nazwa kategorii zgłoszenia (np. AWARIA_WODY, AWARIA_PRADU, USTERKA_WINDA)"),
                new DomainFieldConfig("building", "Budynek", FieldType.TEXT, true,
                        List.of(), "Budynek/lokalizacja, której dotyczy kategoria (np. Budynek A, Budynek B)"),
                new DomainFieldConfig("defaultPriority", "Domyślny priorytet", FieldType.SELECT, true,
                        List.of("LOW", "MEDIUM", "HIGH", "CRITICAL"),
                        "LOW-niski | MEDIUM-średni | HIGH-wysoki | CRITICAL-krytyczny")
        );

        List<DomainFieldConfig> requestFields = List.of(
                new DomainFieldConfig("title", "Tytuł zgłoszenia", FieldType.TEXT, true,
                        List.of(), "Krótki tytuł opisujący problem"),
                new DomainFieldConfig("description", "Opis zgłoszenia", FieldType.TEXTAREA, true,
                        List.of(), "Szczegółowy opis usterki lub problemu"),
                new DomainFieldConfig("status", "Status zgłoszenia", FieldType.SELECT, true,
                        List.of("REPORTED", "IN_PROGRESS", "RESOLVED", "CLOSED"),
                        "REPORTED-zgłoszony | IN_PROGRESS-w trakcie | RESOLVED-rozwiązany | CLOSED-zamknięty"),
                new DomainFieldConfig("location", "Lokalizacja", FieldType.TEXT, true,
                        List.of(), "Dokładne miejsce usterki (np. klatka schodowa, piętro 3, parking)"),
                new DomainFieldConfig("tenantName", "Imię i nazwisko", FieldType.TEXT, true,
                        List.of(), "Imię i nazwisko zgłaszającego mieszkańca"),
                new DomainFieldConfig("reportedBy", "Zgłaszający", FieldType.TEXT, true,
                        List.of(), "Imię i nazwisko osoby zgłaszającej"),
                new DomainFieldConfig("contactPhone", "Telefon kontaktowy", FieldType.TEXT, true,
                        List.of(), "Numer telefonu do kontaktu"),
                new DomainFieldConfig("urgency", "Pilność", FieldType.SELECT, true,
                        List.of("LOW", "MEDIUM", "HIGH", "IMMEDIATE"),
                        "LOW-niski | MEDIUM-średni | HIGH-wysoki | IMMEDIATE-natychmiastowy"),
                new DomainFieldConfig("additionalInfo", "Dodatkowe informacje", FieldType.TEXTAREA, false,
                        List.of(), "Dodatkowe szczegóły zgłoszenia")
        );

        return new DomainProfile(
                "System zgłoszeń osiedlowych",
                "Kategoria", "Kategorie",
                "Zgłoszenie", "Zgłoszenia",
                "PLN",
                AlgorithmMode.VALUE_CALCULATION_ONLY,
                PricingUnit.FLAT,
                false,
                false,
                resourceFields,
                requestFields
        );
    }
}
