package pl.zpo.app.users;

/**
 * Returned by register/login. {@code token} is a JWT to be sent as
 * {@code Authorization: Bearer <token>} on subsequent requests.
 */
public record AuthResponse(
        String token,
        String tokenType,
        long expiresIn,
        UserDto user
) {

    public static AuthResponse of(String token, long expiresInSeconds, UserEntity user) {
        return new AuthResponse(token, "Bearer", expiresInSeconds, UserDto.from(user));
    }
}
