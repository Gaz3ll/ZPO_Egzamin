package com.fitness.service;

import com.fitness.model.FitnessClass;
import com.fitness.model.Registration;
import com.fitness.repository.FitnessClassRepository;
import com.fitness.repository.RegistrationRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.IntStream;

/**
 * Serwis obsługujący rejestrację użytkowników na zajęcia fitness.
 * Logika sterowania została przepisana w paradygmacie funkcyjnym bez użycia instrukcji 'if' oraz pętli.
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
     * Zapisuje użytkownika na zajęcia fitness (podejście funkcyjne).
     */
    @Transactional
    public String register(String userId, Long classId) {
        FitnessClass fitnessClass = classRepository.findById(classId)
            .orElseThrow(() -> new RuntimeException("Class not found"));

        // Brak if - użycie Optional.map() oraz orElseGet() z wyrażeniem trójargumentowym
        return registrationRepository.findByUserIdAndClassId(userId, classId)
            .map(existing -> "Already registered")
            .orElseGet(() -> {
                long mainCount = registrationRepository.countByClassIdAndStatus(classId, "MAIN");
                return mainCount < fitnessClass.getMaxCapacity() ?
                    saveAndReturn(new Registration(userId, classId, "MAIN", null), "Registered on main list") :
                    saveAndReturn(new Registration(userId, classId, "WAITING", (int) registrationRepository.countByClassIdAndStatus(classId, "WAITING") + 1), 
                        "Registered on waiting list, position " + (registrationRepository.countByClassIdAndStatus(classId, "WAITING") + 1));
            });
    }

    private String saveAndReturn(Registration reg, String msg) {
        registrationRepository.save(reg);
        return msg;
    }

    /**
     * Wypisuje użytkownika z zajęć (podejście funkcyjne).
     */
    @Transactional
    public String unregister(String userId, Long classId) {
        Registration reg = registrationRepository.findByUserIdAndClassId(userId, classId)
            .orElseThrow(() -> new RuntimeException("Registration not found"));

        registrationRepository.delete(reg);

        // Brak if - wyrażenie warunkowe decydujące o wywołaniu odpowiedniej metody
        return "MAIN".equals(reg.getStatus()) ?
            promoteAndReturn(classId, "Unregistered from main list") :
            reorderAndReturn(classId, "Unregistered from waiting list");
    }

    private String promoteAndReturn(Long classId, String msg) {
        promoteFromWaitingList(classId);
        return msg;
    }

    private String reorderAndReturn(Long classId, String msg) {
        reorderWaitingList(classId);
        return msg;
    }

    /**
     * Promuje pierwszą osobę z kolejki oczekujących za pomocą Optional (bez if).
     */
    private void promoteFromWaitingList(Long classId) {
        List<Registration> waiting = registrationRepository.findByClassIdAndStatusOrderByPosition(classId, "WAITING");
        
        // Brak if - używamy streamu i ifPresent do obsłużenia pierwszego elementu
        waiting.stream().findFirst().ifPresent(promoted -> {
            promoted.setStatus("MAIN");
            promoted.setPosition(null);
            registrationRepository.save(promoted);
            reorderWaitingList(classId);
        });
    }

    /**
     * Reindeksuje pozycje na liście oczekujących za pomocą IntStream (bez pętli for).
     */
    private void reorderWaitingList(Long classId) {
        List<Registration> waiting = registrationRepository.findByClassIdAndStatusOrderByPosition(classId, "WAITING");
        
        // Brak pętli for - używamy IntStream.range() do operacji indeksowanych
        IntStream.range(0, waiting.size())
            .forEach(i -> {
                waiting.get(i).setPosition(i + 1);
                registrationRepository.save(waiting.get(i));
            });
    }

    public List<Registration> getRegistrationsForClass(Long classId) {
        return registrationRepository.findByClassIdAndStatusOrderByPosition(classId, "MAIN");
    }
}
