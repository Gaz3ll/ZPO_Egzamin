package pl.zpo.app.domain.config;

import java.util.List;
import org.springframework.stereotype.Component;

@Component
public class DomainProfileProvider {

    public DomainProfile getActiveProfile() {
        List<DomainFieldConfig> resourceFields = List.of(
                new DomainFieldConfig("roomName", "Nazwa sali", FieldType.TEXT, true,
                        List.of(), "Nazwa sali konferencyjnej (np. Sala A, Sala VIP)"),
                new DomainFieldConfig("capacity", "Pojemność", FieldType.NUMBER, true,
                        List.of(), "Maksymalna liczba osób"),
                new DomainFieldConfig("floor", "Piętro", FieldType.TEXT, false,
                        List.of(), "Piętro (np. 1, 2, 3)"),
                new DomainFieldConfig("hasProjector", "Projektor", FieldType.BOOLEAN, false,
                        List.of(), "Czy sala ma projektor"),
                new DomainFieldConfig("hasVideoConference", "Wideokonferencja", FieldType.BOOLEAN, false,
                        List.of(), "Czy sala ma sprzęt do wideokonferencji"),
                new DomainFieldConfig("dailyRate", "Stawka dzienna (PLN)", FieldType.NUMBER, true,
                        List.of(), "Koszt wynajmu sali za jeden dzień")
        );

        List<DomainFieldConfig> requestFields = List.of(
                new DomainFieldConfig("renterName", "Imię i nazwisko", FieldType.TEXT, true,
                        List.of(), "Osoba rezerwująca salę"),
                new DomainFieldConfig("renterEmail", "Email", FieldType.TEXT, true,
                        List.of(), "Kontaktowy adres email"),
                new DomainFieldConfig("renterPhone", "Telefon", FieldType.TEXT, true,
                        List.of(), "Numer telefonu kontaktowego"),
                new DomainFieldConfig("meetingTitle", "Tytuł spotkania", FieldType.TEXT, true,
                        List.of(), "Tytuł spotkania (np. 'Szkolenie BHP')"),
                new DomainFieldConfig("attendeeCount", "Liczba uczestników", FieldType.NUMBER, true,
                        List.of(), "Planowana liczba osób"),
                new DomainFieldConfig("notes", "Uwagi", FieldType.TEXTAREA, false,
                        List.of(), "Dodatkowe informacje")
        );

        return new DomainProfile(
                "System rezerwacji salek konferencyjnych",
                "Sala", "Sale",
                "Rezerwacja", "Rezerwacje",
                "PLN",
                AlgorithmMode.TIME_AVAILABILITY_AND_CALCULATION,
                PricingUnit.PER_DAY,
                /* requiresTimeWindow */ true,
                /* requiresQuantity */ false,
                resourceFields,
                requestFields
        );
    }
}
