package com.fitness.service;

import com.fitness.model.FitnessClass;
import com.fitness.model.Registration;
import com.fitness.repository.FitnessClassRepository;
import com.fitness.repository.RegistrationRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * Serwis obsługujący rejestrację użytkowników na zajęcia fitness.
 * Zarządza również kolejkowaniem (listami rezerwowymi).
 */
@Service
public class RegistrationService {

    private final FitnessClassRepository classRepository;
    private final RegistrationRepository registrationRepository;

    public RegistrationService(FitnessClassRepository classRepository, RegistrationRepository registrationRepository) {
        this.classRepository = classRepository;
        this.registrationRepository = registrationRepository;
    }

    public List<FitnessClass> getAllClasses() {
        return classRepository.findAll();
    }

    /**
     * Zapisuje użytkownika na zajęcia fitness.
     * Jeśli limit miejsc jest wyczerpany, zapisuje na listę rezerwową (WAITING) z odpowiednią pozycją.
     */
    @Transactional
    public String register(String userId, Long classId) {
        FitnessClass fitnessClass = classRepository.findById(classId)
            .orElseThrow(() -> new RuntimeException("Class not found"));

        // Sprawdzenie, czy użytkownik jest już zapisany na te zajęcia
        var existing = registrationRepository.findByUserIdAndClassId(userId, classId);
        if (existing.isPresent()) {
            return "Already registered";
        }

        // Pobranie liczby osób na liście głównej
        long mainCount = registrationRepository.countByClassIdAndStatus(classId, "MAIN");

        if (mainCount < fitnessClass.getMaxCapacity()) {
            // Zapis na listę główną
            Registration reg = new Registration(userId, classId, "MAIN", null);
            registrationRepository.save(reg);
            return "Registered on main list";
        } else {
            // Brak wolnych miejsc - zapis na listę rezerwową (pozycja = obecny rozmiar kolejki + 1)
            long waitingCount = registrationRepository.countByClassIdAndStatus(classId, "WAITING");
            Registration reg = new Registration(userId, classId, "WAITING", (int) waitingCount + 1);
            registrationRepository.save(reg);
            return "Registered on waiting list, position " + (waitingCount + 1);
        }
    }

    /**
     * Wypisuje użytkownika z zajęć fitness.
     * Usunięcie z listy głównej powoduje automatyczne wciągnięcie pierwszej osoby z listy rezerwowej.
     * Usunięcie z listy rezerwowej powoduje przenumerowanie pozostałych rezerwowych.
     */
    @Transactional
    public String unregister(String userId, Long classId) {
        Registration reg = registrationRepository.findByUserIdAndClassId(userId, classId)
            .orElseThrow(() -> new RuntimeException("Registration not found"));

        boolean wasMain = "MAIN".equals(reg.getStatus());
        registrationRepository.delete(reg);

        if (wasMain) {
            // Jeśli wypisana osoba była na liście głównej, wciągamy pierwszą osobę z rezerwy
            promoteFromWaitingList(classId);
            return "Unregistered from main list";
        } else {
            // Jeśli wypisana osoba była na liście rezerwowej, reindeksujemy pozycje pozostałych w kolejce
            reorderWaitingList(classId);
            return "Unregistered from waiting list";
        }
    }

    /**
     * Promuje pierwszego użytkownika z listy rezerwowej na listę główną.
     */
    private void promoteFromWaitingList(Long classId) {
        List<Registration> waiting = registrationRepository.findByClassIdAndStatusOrderByPosition(classId, "WAITING");
        if (!waiting.isEmpty()) {
            Registration promoted = waiting.get(0);
            promoted.setStatus("MAIN");
            promoted.setPosition(null); // Status MAIN nie posiada pozycji w kolejce
            registrationRepository.save(promoted);
            // Reindeksujemy pozostałą listę rezerwową
            reorderWaitingList(classId);
        }
    }

    /**
     * Aktualizuje pozycje (reindeksuje) użytkowników na liście rezerwowej po usunięciu/promocji kogoś.
     */
    private void reorderWaitingList(Long classId) {
        List<Registration> waiting = registrationRepository.findByClassIdAndStatusOrderByPosition(classId, "WAITING");
        for (int i = 0; i < waiting.size(); i++) {
            waiting.get(i).setPosition(i + 1);
            registrationRepository.save(waiting.get(i));
        }
    }

    public List<Registration> getRegistrationsForClass(Long classId) {
        return registrationRepository.findByClassIdAndStatusOrderByPosition(classId, "MAIN");
    }
}
