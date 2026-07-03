package pl.zpo.app.users;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import pl.zpo.app.exception.ConflictException;
import pl.zpo.app.exception.NotFoundException;
import pl.zpo.app.security.PasswordService;

/**
 * User lifecycle operations. Password hashing is delegated to {@link PasswordService};
 * no other class stores raw or hashed passwords directly.
 */
@Service
@Transactional
public class UserService {

    private final UserRepository userRepository;
    private final PasswordService passwordService;

    public UserService(UserRepository userRepository, PasswordService passwordService) {
        this.userRepository = userRepository;
        this.passwordService = passwordService;
    }

    /** Create a user with a BCrypt-hashed password. Rejects duplicate emails with 409. */
    public UserEntity createUser(String name, String email, String rawPassword, Role role) {
        String normalizedEmail = email.trim().toLowerCase();
        if (userRepository.existsByEmail(normalizedEmail)) {
            throw new ConflictException("A user with email '" + normalizedEmail + "' already exists");
        }
        UserEntity user = new UserEntity(name, normalizedEmail, passwordService.hash(rawPassword), role);
        return userRepository.save(user);
    }

    /**
     * Create or refresh a demo user used by seed data. This keeps preset switching idempotent:
     * existing demo accounts get the expected role and password instead of failing on duplicates.
     */
    public UserEntity ensureUser(String name, String email, String rawPassword, Role role) {
        String normalizedEmail = email.trim().toLowerCase();
        return userRepository.findByEmail(normalizedEmail)
                .map(existing -> {
                    existing.setName(name);
                    existing.setRole(role);
                    existing.setPasswordHash(passwordService.hash(rawPassword));
                    return userRepository.save(existing);
                })
                .orElseGet(() -> userRepository.save(
                        new UserEntity(name, normalizedEmail, passwordService.hash(rawPassword), role)));
    }

    @Transactional(readOnly = true)
    public UserEntity getById(Long id) {
        return userRepository.findById(id)
                .orElseThrow(() -> NotFoundException.of("User", id));
    }

    @Transactional(readOnly = true)
    public UserEntity getByEmail(String email) {
        return userRepository.findByEmail(email.trim().toLowerCase())
                .orElseThrow(() -> new NotFoundException("User with email '" + email + "' was not found"));
    }
}
