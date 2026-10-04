package lk.serenetravels.userservice.service;

import lk.serenetravels.userservice.dto.CreateUserRequest;
import org.keycloak.representations.idm.UserRepresentation;

public interface KeycloakUserService {

    String createUser(CreateUserRequest request, boolean temporaryPassword, boolean enabled);

    UserRepresentation getUser(String keycloakUserId);

    void setEnabled(String keycloakUserId, boolean enabled);

    void assignRealmRole(String keycloakUserId, String roleName);

    void deleteUser(String keycloakUserId);
}
