package pl.zpo.app.users;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import pl.zpo.app.common.ApiResponse;

/**
 * Authentication endpoints. Controllers stay thin: validate input, delegate to the service,
 * wrap the result in {@link ApiResponse}.
 */
@RestController
@RequestMapping("/api/auth")
@Tag(name = "Auth", description = "Registration, login and current-user endpoints")
public class AuthController {

    private final AuthService authService;

    public AuthController(AuthService authService) {
        this.authService = authService;
    }

    @PostMapping("/register")
    @Operation(summary = "Register a new account (role USER) and receive a JWT")
    public ApiResponse<AuthResponse> register(@Valid @RequestBody RegisterRequest request) {
        return ApiResponse.ok(authService.register(request));
    }

    @PostMapping("/login")
    @Operation(summary = "Authenticate with email + password and receive a JWT")
    public ApiResponse<AuthResponse> login(@Valid @RequestBody LoginRequest request) {
        return ApiResponse.ok(authService.login(request));
    }

    @GetMapping("/me")
    @Operation(summary = "Current authenticated user's profile", security = @SecurityRequirement(name = "bearerAuth"))
    public ApiResponse<UserDto> me() {
        return ApiResponse.ok(authService.currentUser());
    }
}
