package lk.serenetravels.userservice.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lk.serenetravels.userservice.entity.UserType;

public class CreateUserRequest {

    @Schema(example = "janith.thiwanka")
    @NotBlank
    private String username;

    @Schema(example = "janith.tw@gmail.com")
    @NotBlank
    @Email
    private String email;

    @Schema(example = "Janith")
    @NotBlank
    private String firstName;

    @Schema(example = "Thiwanka")
    @NotBlank
    private String lastName;

    @Schema(example = "Secret@123", description = "Set as a permanent password; the user can log in with it immediately")
    @NotBlank
    private String password;

    @Schema(description = "Any user type is allowed here, unlike self-registration which is always AGENT")
    @NotNull
    private UserType userType;

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

    public UserType getUserType() {
        return userType;
    }

    public void setUserType(UserType userType) {
        this.userType = userType;
    }
}
