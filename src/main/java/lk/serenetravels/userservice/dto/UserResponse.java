package lk.serenetravels.userservice.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lk.serenetravels.userservice.entity.UserType;

import java.util.UUID;

public class UserResponse {

    private final UUID id;
    private final String keycloakUserId;
    private final String username;
    private final String email;
    private final String firstName;
    private final String lastName;
    private final UserType userType;

    @Schema(description = "One-time registration confirmation by an admin; false locks the account out of login")
    private final boolean verified;

    @Schema(description = "Ongoing account lifecycle flag (e.g. disabled because the person left), independent of verified")
    private final boolean active;

    @Schema(description = "Id of the MANAGER this AGENT reports to; null if unassigned or not an AGENT")
    private final UUID managerId;

    @Schema(description = "Id of the partner company this AGENT works for; null if unassigned or not an AGENT")
    private final UUID companyId;

    public UserResponse(UUID id, String keycloakUserId, String username, String email,
                         String firstName, String lastName, UserType userType, boolean verified,
                         boolean active, UUID managerId, UUID companyId) {
        this.id = id;
        this.keycloakUserId = keycloakUserId;
        this.username = username;
        this.email = email;
        this.firstName = firstName;
        this.lastName = lastName;
        this.userType = userType;
        this.verified = verified;
        this.active = active;
        this.managerId = managerId;
        this.companyId = companyId;
    }

    public UUID getId() {
        return id;
    }

    public String getKeycloakUserId() {
        return keycloakUserId;
    }

    public String getUsername() {
        return username;
    }

    public String getEmail() {
        return email;
    }

    public String getFirstName() {
        return firstName;
    }

    public String getLastName() {
        return lastName;
    }

    public UserType getUserType() {
        return userType;
    }

    public boolean isVerified() {
        return verified;
    }

    public boolean isActive() {
        return active;
    }

    public UUID getManagerId() {
        return managerId;
    }

    public UUID getCompanyId() {
        return companyId;
    }
}
