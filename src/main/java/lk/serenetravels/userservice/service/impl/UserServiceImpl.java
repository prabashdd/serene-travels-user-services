package lk.serenetravels.userservice.service.impl;

import lk.serenetravels.userservice.dto.CreateUserRequest;
import lk.serenetravels.userservice.dto.UpdateUserProfileRequest;
import lk.serenetravels.userservice.dto.UserResponse;
import lk.serenetravels.userservice.entity.UserProfile;
import lk.serenetravels.userservice.entity.UserType;
import lk.serenetravels.userservice.exception.InvalidManagerAssignmentException;
import lk.serenetravels.userservice.exception.UserNotFoundException;
import lk.serenetravels.userservice.repository.UserProfileRepository;
import lk.serenetravels.userservice.service.KeycloakUserService;
import lk.serenetravels.userservice.service.UserService;
import lk.serenetravels.userservice.util.UserProfileSpecifications;
import org.keycloak.representations.idm.UserRepresentation;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.util.List;
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
                    request.getFirstName(), request.getLastName(), request.getUserType(), profile.isVerified(),
                    profile.isActive(), profile.getManagerId());
        } catch (RuntimeException ex) {
            // Keycloak and Postgres aren't in the same transaction, so a failure past this
            // point would otherwise leave an orphaned Keycloak user with no local profile.
            rollbackKeycloakUser(keycloakUserId);
            throw ex;
        }
    }

    @Override
    public UserResponse verifyUser(UUID id) {
        UserProfile profile = findProfileOrThrow(id);
        profile.setVerified(true);
        userProfileRepository.save(profile);
        syncKeycloakEnabled(profile);
        return toResponse(profile);
    }

    @Override
    public UserResponse getById(UUID id) {
        return toResponse(findProfileOrThrow(id));
    }

    @Override
    public UserResponse getByKeycloakUserId(String keycloakUserId) {
        UserProfile profile = userProfileRepository.findByKeycloakUserId(keycloakUserId)
                .orElseThrow(() -> new UserNotFoundException(
                        "No user profile found for the authenticated account"));
        return toResponse(profile);
    }

    @Override
    public Page<UserResponse> list(UserType userType, Boolean verified, Boolean active, Pageable pageable) {
        return userProfileRepository.findAll(UserProfileSpecifications.filter(userType, verified, active), pageable)
                .map(this::toResponse);
    }

    @Override
    public UserResponse updateProfile(UUID id, UpdateUserProfileRequest request) {
        UserProfile profile = findProfileOrThrow(id);
        keycloakUserService.updateProfile(profile.getKeycloakUserId(), request.getEmail(),
                request.getFirstName(), request.getLastName());
        return toResponse(profile);
    }

    @Override
    public UserResponse assignManager(UUID agentId, UUID managerId) {
        UserProfile agent = findProfileOrThrow(agentId);
        if (agent.getUserType() != UserType.AGENT) {
            throw new InvalidManagerAssignmentException("Only AGENT users can be assigned a manager");
        }

        if (managerId != null) {
            UserProfile manager = findProfileOrThrow(managerId);
            if (manager.getUserType() != UserType.MANAGER) {
                throw new InvalidManagerAssignmentException(
                        "User '" + managerId + "' is not a MANAGER and cannot be assigned as one");
            }
        }

        agent.setManagerId(managerId);
        userProfileRepository.save(agent);
        return toResponse(agent);
    }

    @Override
    public List<UserResponse> getTeam(UUID managerId) {
        UserProfile manager = findProfileOrThrow(managerId);
        if (manager.getUserType() != UserType.MANAGER) {
            throw new InvalidManagerAssignmentException("User '" + managerId + "' is not a MANAGER");
        }
        return userProfileRepository.findByManagerId(managerId).stream()
                .map(this::toResponse)
                .toList();
    }

    @Override
    public UserResponse deactivate(UUID id) {
        UserProfile profile = findProfileOrThrow(id);
        profile.setActive(false);
        userProfileRepository.save(profile);
        syncKeycloakEnabled(profile);
        return toResponse(profile);
    }

    @Override
    public UserResponse reactivate(UUID id) {
        UserProfile profile = findProfileOrThrow(id);
        profile.setActive(true);
        userProfileRepository.save(profile);
        syncKeycloakEnabled(profile);
        return toResponse(profile);
    }

    @Override
    public UserResponse changeUserType(UUID id, UserType newType) {
        UserProfile profile = findProfileOrThrow(id);
        UserType oldType = profile.getUserType();

        if (oldType != newType) {
            keycloakUserService.removeRealmRole(profile.getKeycloakUserId(), oldType.name());
            keycloakUserService.assignRealmRole(profile.getKeycloakUserId(), newType.name());
        }

        profile.setUserType(newType);
        if (newType != UserType.AGENT) {
            // A manager assignment only makes sense for an AGENT.
            profile.setManagerId(null);
        }
        userProfileRepository.save(profile);
        return toResponse(profile);
    }

    private UserProfile findProfileOrThrow(UUID id) {
        return userProfileRepository.findById(id)
                .orElseThrow(() -> new UserNotFoundException("No user profile found with id '" + id + "'"));
    }

    // Keycloak's enabled flag always mirrors verified && active: a self-registered
    // user needs admin verification, and any user can separately be deactivated
    // for lifecycle reasons (e.g. having left) independent of that verification.
    private void syncKeycloakEnabled(UserProfile profile) {
        keycloakUserService.setEnabled(profile.getKeycloakUserId(), profile.isVerified() && profile.isActive());
    }

    private UserResponse toResponse(UserProfile profile) {
        UserRepresentation keycloakUser = keycloakUserService.getUser(profile.getKeycloakUserId());
        return new UserResponse(profile.getId(), profile.getKeycloakUserId(), keycloakUser.getUsername(),
                keycloakUser.getEmail(), keycloakUser.getFirstName(), keycloakUser.getLastName(),
                profile.getUserType(), profile.isVerified(), profile.isActive(), profile.getManagerId());
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
