package com.fitness;

import com.fitness.model.FitnessClass;
import com.fitness.model.Registration;
import com.fitness.repository.FitnessClassRepository;
import com.fitness.repository.RegistrationRepository;
import com.fitness.service.RegistrationService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
class RegistrationServiceTest {

    @Autowired
    private RegistrationService registrationService;

    @Autowired
    private FitnessClassRepository classRepository;

    @Autowired
    private RegistrationRepository registrationRepository;

    private Long classId;

    @BeforeEach
    void setUp() {
        registrationRepository.deleteAll();
        classRepository.deleteAll();
        FitnessClass fc = classRepository.save(new FitnessClass("Yoga", "Monday", "08:00", 2));
        classId = fc.getId();
    }

    @Test
    void testRegisterMainList() {
        String result = registrationService.register("user1", classId);
        assertEquals("Registered on main list", result);
        long mainCount = registrationRepository.countByClassIdAndStatus(classId, "MAIN");
        assertEquals(1, mainCount);
    }

    @Test
    void testRegisterWaitingList() {
        registrationService.register("user1", classId);
        registrationService.register("user2", classId);
        String result = registrationService.register("user3", classId);
        assertEquals("Registered on waiting list, position 1", result);
        long waitingCount = registrationRepository.countByClassIdAndStatus(classId, "WAITING");
        assertEquals(1, waitingCount);
    }

    @Test
    void testPromotionFromWaitingList() {
        registrationService.register("user1", classId);
        registrationService.register("user2", classId);
        registrationService.register("user3", classId);
        registrationService.register("user4", classId);

        assertEquals(2, registrationRepository.countByClassIdAndStatus(classId, "MAIN"));
        assertEquals(2, registrationRepository.countByClassIdAndStatus(classId, "WAITING"));

        registrationService.unregister("user1", classId);

        List<Registration> mainList = registrationRepository.findByClassIdAndStatusOrderByPosition(classId, "MAIN");
        assertEquals(2, mainList.size());
        assertEquals("user3", mainList.get(1).getUserId());

        List<Registration> waitingList = registrationRepository.findByClassIdAndStatusOrderByPosition(classId, "WAITING");
        assertEquals(1, waitingList.size());
        assertEquals("user4", waitingList.get(0).getUserId());
        assertEquals(1, waitingList.get(0).getPosition());
    }

    @Test
    void testUnregisterFromWaitingList() {
        registrationService.register("user1", classId);
        registrationService.register("user2", classId);
        registrationService.register("user3", classId);
        registrationService.register("user4", classId);

        String result = registrationService.unregister("user4", classId);
        assertEquals("Unregistered from waiting list", result);

        List<Registration> waitingList = registrationRepository.findByClassIdAndStatusOrderByPosition(classId, "WAITING");
        assertEquals(1, waitingList.size());
        assertEquals("user3", waitingList.get(0).getUserId());
        assertEquals(1, waitingList.get(0).getPosition());
    }

    @Test
    void testMultiplePromotions() {
        registrationService.register("user1", classId);
        registrationService.register("user2", classId);
        registrationService.register("user3", classId);
        registrationService.register("user4", classId);

        registrationService.unregister("user1", classId);
        registrationService.unregister("user2", classId);

        List<Registration> mainList = registrationRepository.findByClassIdAndStatusOrderByPosition(classId, "MAIN");
        assertEquals(2, mainList.size());
        assertEquals("user3", mainList.get(0).getUserId());
        assertEquals("user4", mainList.get(1).getUserId());

        assertEquals(0, registrationRepository.countByClassIdAndStatus(classId, "WAITING"));
    }
}
