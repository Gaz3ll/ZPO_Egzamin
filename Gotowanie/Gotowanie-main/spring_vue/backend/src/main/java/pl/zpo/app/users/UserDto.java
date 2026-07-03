package pl.zpo.app.users;

/** Public representation of a user (never exposes the password hash). */
public record UserDto(Long id, String name, String email, Role role) {

    public static UserDto from(UserEntity user) {
        return new UserDto(user.getId(), user.getName(), user.getEmail(), user.getRole());
    }
}
