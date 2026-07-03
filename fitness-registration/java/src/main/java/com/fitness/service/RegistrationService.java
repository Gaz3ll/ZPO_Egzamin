package com.fitness.service;

import com.fitness.model.FitnessClass;
import com.fitness.model.Registration;
import com.fitness.repository.FitnessClassRepository;
import com.fitness.repository.RegistrationRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

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

    @Transactional
    public String register(String userId, Long classId) {
        FitnessClass fitnessClass = classRepository.findById(classId)
            .orElseThrow(() -> new RuntimeException("Class not found"));

        var existing = registrationRepository.findByUserIdAndClassId(userId, classId);
        if (existing.isPresent()) {
            return "Already registered";
        }

        long mainCount = registrationRepository.countByClassIdAndStatus(classId, "MAIN");

        if (mainCount < fitnessClass.getMaxCapacity()) {
            Registration reg = new Registration(userId, classId, "MAIN", null);
            registrationRepository.save(reg);
            return "Registered on main list";
        } else {
            long waitingCount = registrationRepository.countByClassIdAndStatus(classId, "WAITING");
            Registration reg = new Registration(userId, classId, "WAITING", (int) waitingCount + 1);
            registrationRepository.save(reg);
            return "Registered on waiting list, position " + (waitingCount + 1);
        }
    }

    @Transactional
    public String unregister(String userId, Long classId) {
        Registration reg = registrationRepository.findByUserIdAndClassId(userId, classId)
            .orElseThrow(() -> new RuntimeException("Registration not found"));

        registrationRepository.delete(reg);

        if ("MAIN".equals(reg.getStatus())) {
            promoteFromWaitingList(classId);
            return "Unregistered from main list";
        } else {
            reorderWaitingList(classId);
            return "Unregistered from waiting list";
        }
    }

    private void promoteFromWaitingList(Long classId) {
        List<Registration> waiting = registrationRepository.findByClassIdAndStatusOrderByPosition(classId, "WAITING");
        if (!waiting.isEmpty()) {
            Registration promoted = waiting.get(0);
            promoted.setStatus("MAIN");
            promoted.setPosition(null);
            registrationRepository.save(promoted);
            reorderWaitingList(classId);
        }
    }

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
