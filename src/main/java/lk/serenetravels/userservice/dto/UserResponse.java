package lk.serenetravels.userservice.dto;

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
    private final boolean verified;

    public UserResponse(UUID id, String keycloakUserId, String username, String email,
                         String firstName, String lastName, UserType userType, boolean verified) {
        this.id = id;
        this.keycloakUserId = keycloakUserId;
        this.username = username;
        this.email = email;
        this.firstName = firstName;
        this.lastName = lastName;
        this.userType = userType;
        this.verified = verified;
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
}
