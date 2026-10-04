package lk.serenetravels.userservice.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

public class UpdateUserProfileRequest {

    @Schema(example = "Kasun")
    @NotBlank
    private String firstName;

    @Schema(example = "Silva")
    @NotBlank
    private String lastName;

    @Schema(example = "kasun.silva@example.com")
    @NotBlank
    @Email
    private String email;

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

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }
}
