package lk.serenetravels.userservice.service.impl;

import lk.serenetravels.userservice.dto.CreateUserRequest;
import lk.serenetravels.userservice.dto.UserResponse;
import lk.serenetravels.userservice.entity.UserProfile;
import lk.serenetravels.userservice.exception.UserNotFoundException;
import lk.serenetravels.userservice.repository.UserProfileRepository;
import lk.serenetravels.userservice.service.KeycloakUserService;
import lk.serenetravels.userservice.service.UserService;
import org.keycloak.representations.idm.UserRepresentation;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Service
public class UserServiceImpl implements UserService {

    private static final Logger log = LoggerFactory.getLogger(UserServiceImpl.class);

    private final KeycloakUserService keycloakUserService;
    private final UserProfileRepository userProfileRepository;

    public UserServiceImpl(KeycloakUserService keycloakUserService, UserProfileRepository userProfileRepository) {
        this.keycloakUserService = keycloakUserService;
        this.userProfileRepository = userProfileRepository;
    }

    @Override
    public UserResponse createUser(CreateUserRequest request, boolean temporaryPassword, boolean verified) {
        String keycloakUserId = keycloakUserService.createUser(request, temporaryPassword, verified);

        try {
            keycloakUserService.assignRealmRole(keycloakUserId, request.getUserType().name());

            UserProfile profile = new UserProfile(keycloakUserId, request.getUserType(), verified);
            userProfileRepository.save(profile);

            return new UserResponse(profile.getId(), keycloakUserId, request.getUsername(), request.getEmail(),
                    request.getFirstName(), request.getLastName(), request.getUserType(), profile.isVerified());
        } catch (RuntimeException ex) {
            // Keycloak and Postgres aren't in the same transaction, so a failure past this
            // point would otherwise leave an orphaned Keycloak user with no local profile.
            rollbackKeycloakUser(keycloakUserId);
            throw ex;
        }
    }

    @Override
    public UserResponse verifyUser(UUID id) {
        UserProfile profile = userProfileRepository.findById(id)
                .orElseThrow(() -> new UserNotFoundException("No user profile found with id '" + id + "'"));
        profile.setVerified(true);
        userProfileRepository.save(profile);
        keycloakUserService.setEnabled(profile.getKeycloakUserId(), true);

        UserRepresentation keycloakUser = keycloakUserService.getUser(profile.getKeycloakUserId());
        return new UserResponse(profile.getId(), profile.getKeycloakUserId(), keycloakUser.getUsername(),
                keycloakUser.getEmail(), keycloakUser.getFirstName(), keycloakUser.getLastName(),
                profile.getUserType(), profile.isVerified());
    }

    private void rollbackKeycloakUser(String keycloakUserId) {
        try {
            keycloakUserService.deleteUser(keycloakUserId);
        } catch (RuntimeException rollbackEx) {
            log.error("Failed to roll back orphaned Keycloak user {} after a failed create-user request",
                    keycloakUserId, rollbackEx);
        }
    }
}
