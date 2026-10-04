package lk.serenetravels.userservice.service;

import lk.serenetravels.userservice.dto.CreateUserRequest;
import org.keycloak.representations.idm.UserRepresentation;

import java.util.Optional;

public interface KeycloakUserService {

    String createUser(CreateUserRequest request, boolean temporaryPassword, boolean enabled);

    UserRepresentation getUser(String keycloakUserId);

    Optional<UserRepresentation> findByUsernameOrEmail(String usernameOrEmail);

    void setEnabled(String keycloakUserId, boolean enabled);

    void updateProfile(String keycloakUserId, String email, String firstName, String lastName);

    void setPassword(String keycloakUserId, String password, boolean temporary);

    void assignRealmRole(String keycloakUserId, String roleName);

    void removeRealmRole(String keycloakUserId, String roleName);

    void sendPasswordResetEmail(String keycloakUserId);

    void deleteUser(String keycloakUserId);
}
