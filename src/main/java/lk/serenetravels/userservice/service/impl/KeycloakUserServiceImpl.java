package lk.serenetravels.userservice.service.impl;

import jakarta.ws.rs.core.Response;
import lk.serenetravels.userservice.config.KeycloakProperties;
import lk.serenetravels.userservice.dto.CreateUserRequest;
import lk.serenetravels.userservice.exception.KeycloakAdminException;
import lk.serenetravels.userservice.exception.UserAlreadyExistsException;
import lk.serenetravels.userservice.service.KeycloakUserService;
import org.keycloak.admin.client.Keycloak;
import org.keycloak.admin.client.resource.RealmResource;
import org.keycloak.admin.client.resource.UsersResource;
import org.keycloak.representations.idm.CredentialRepresentation;
import org.keycloak.representations.idm.RoleRepresentation;
import org.keycloak.representations.idm.UserRepresentation;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.util.List;

@Service
public class KeycloakUserServiceImpl implements KeycloakUserService {

    private final Keycloak keycloak;
    private final KeycloakProperties properties;

    public KeycloakUserServiceImpl(Keycloak keycloak, KeycloakProperties properties) {
        this.keycloak = keycloak;
        this.properties = properties;
    }

    private RealmResource realm() {
        return keycloak.realm(properties.getRealm());
    }

    @Override
    public String createUser(CreateUserRequest request, boolean temporaryPassword, boolean enabled) {
        UserRepresentation user = new UserRepresentation();
        user.setUsername(request.getUsername());
        user.setEmail(request.getEmail());
        user.setFirstName(request.getFirstName());
        user.setLastName(request.getLastName());
        // Unverified self-registrations are created disabled so they cannot
        // obtain a token at all until an admin verifies the account.
        user.setEnabled(enabled);
        user.setEmailVerified(false);

        CredentialRepresentation credential = new CredentialRepresentation();
        credential.setType(CredentialRepresentation.PASSWORD);
        credential.setValue(request.getPassword());
        // Admin-issued starter passwords force a reset on first login; a password
        // the user just chose themselves at registration does not need that.
        credential.setTemporary(temporaryPassword);
        user.setCredentials(List.of(credential));

        UsersResource usersResource = realm().users();
        try (Response response = usersResource.create(user)) {
            if (response.getStatus() == 201) {
                return extractUserId(response);
            }
            if (response.getStatus() == 409) {
                throw new UserAlreadyExistsException(
                        "A user with username '" + request.getUsername() + "' or email '"
                                + request.getEmail() + "' already exists in Keycloak");
            }
            throw new KeycloakAdminException(
                    "Keycloak user creation failed with status " + response.getStatus());
        }
    }

    @Override
    public UserRepresentation getUser(String keycloakUserId) {
        return realm().users().get(keycloakUserId).toRepresentation();
    }

    @Override
    public void setEnabled(String keycloakUserId, boolean enabled) {
        UserRepresentation user = getUser(keycloakUserId);
        user.setEnabled(enabled);
        realm().users().get(keycloakUserId).update(user);
    }

    @Override
    public void assignRealmRole(String keycloakUserId, String roleName) {
        RoleRepresentation role = realm().roles().get(roleName).toRepresentation();
        realm().users().get(keycloakUserId).roles().realmLevel().add(List.of(role));
    }

    @Override
    public void deleteUser(String keycloakUserId) {
        realm().users().get(keycloakUserId).remove();
    }

    private String extractUserId(Response response) {
        String location = response.getLocation().getPath();
        String[] segments = location.split("/");
        String userId = segments[segments.length - 1];
        if (!StringUtils.hasText(userId)) {
            throw new KeycloakAdminException("Keycloak did not return a user id for the created user");
        }
        return userId;
    }
}
