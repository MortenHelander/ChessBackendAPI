package app.dtos.users;

public record UserRegisterDTO(String username, String firstName, String lastName, String email, String password, String passwordCheck) {
}
