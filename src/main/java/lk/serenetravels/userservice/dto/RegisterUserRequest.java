package lk.serenetravels.userservice.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

public class RegisterUserRequest {

    @Schema(example = "kasun.silva")
    @NotBlank
    private String username;

    @Schema(example = "kasun.silva@example.com")
    @NotBlank
    @Email
    private String email;

    @Schema(example = "Kasun")
    @NotBlank
    private String firstName;

    @Schema(example = "Silva")
    @NotBlank
    private String lastName;

    @Schema(example = "Secret@123")
    @NotBlank
    private String password;

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getFirstName() {
        return firstName;
    }

    public void setFirstName(String firstName) {
        this.firstName = firstName;
    }

    public String getLastName() {
        return lastName;
    }

    public void setLastName(String lastName) {
        this.lastName = lastName;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }
}
