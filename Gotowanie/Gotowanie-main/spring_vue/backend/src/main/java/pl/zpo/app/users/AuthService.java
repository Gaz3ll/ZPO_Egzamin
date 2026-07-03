package pl.zpo.app.users;

import java.util.Optional;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import pl.zpo.app.exception.UnauthorizedException;
import pl.zpo.app.security.CurrentUser;
import pl.zpo.app.security.CurrentUserService;
import pl.zpo.app.security.JwtService;
import pl.zpo.app.security.PasswordService;

/**
 * Authentication use-cases: register, login and "who am I". Login is performed here (find
 * user by email, verify BCrypt hash) rather than through Spring's AuthenticationManager,
 * keeping the flow explicit and easy to follow.
 */
@Service
public class AuthService {

    private final UserService userService;
    private final UserRepository userRepository;
    private final PasswordService passwordService;
    private final JwtService jwtService;
    private final CurrentUserService currentUserService;

    public AuthService(UserService userService,
                       UserRepository userRepository,
                       PasswordService passwordService,
                       JwtService jwtService,
                       CurrentUserService currentUserService) {
        this.userService = userService;
        this.userRepository = userRepository;
        this.passwordService = passwordService;
        this.jwtService = jwtService;
        this.currentUserService = currentUserService;
    }

    /** Register a new self-service account (always role USER) and return a token. */
    @Transactional
    public AuthResponse register(RegisterRequest request) {
        UserEntity user = userService.createUser(
                request.name(), request.email(), request.password(), Role.USER);
        return AuthResponse.of(jwtService.generateToken(user), jwtService.expiresInSeconds(), user);
    }

    /** Verify credentials and return a token. Uses a generic 401 to avoid user enumeration. */
    @Transactional(readOnly = true)
    public AuthResponse login(LoginRequest request) {
        Optional<UserEntity> found = userRepository.findByEmail(request.email().trim().toLowerCase());
        if (found.isEmpty() || !passwordService.matches(request.password(), found.get().getPasswordHash())) {
            throw new UnauthorizedException("Invalid email or password");
        }
        UserEntity user = found.get();
        return AuthResponse.of(jwtService.generateToken(user), jwtService.expiresInSeconds(), user);
    }

    /** Current authenticated user's profile. */
    @Transactional(readOnly = true)
    public UserDto currentUser() {
        CurrentUser current = currentUserService.requireUser();
        return UserDto.from(userService.getById(current.id()));
    }
}
