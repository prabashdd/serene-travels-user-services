package lk.serenetravels.userservice.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

import java.util.UUID;

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

    @Schema(description = "Id of the agent's employer, picked from GET /api/v1/companies/lookup, when that "
            + "company already exists in the system. Provide exactly one of companyId or company, never both.")
    private UUID companyId;

    @Schema(description = "The agent's employer, submitted fresh, when it is not already in the "
            + "GET /api/v1/companies/lookup list. Starts unverified, same as a brand-new company created this "
            + "way always does; needs a separate admin verification from the user's own. Provide exactly one "
            + "of companyId or company, never both.")
    @Valid
    private CreateCompanyRequest company;

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

    public UUID getCompanyId() {
        return companyId;
    }

    public void setCompanyId(UUID companyId) {
        this.companyId = companyId;
    }

    public CreateCompanyRequest getCompany() {
        return company;
    }

    public void setCompany(CreateCompanyRequest company) {
        this.company = company;
    }
}
