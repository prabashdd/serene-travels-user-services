package lk.serenetravels.userservice.service;

import lk.serenetravels.userservice.dto.CreateUserRequest;
import lk.serenetravels.userservice.dto.RegisterUserRequest;
import lk.serenetravels.userservice.dto.UpdateUserProfileRequest;
import lk.serenetravels.userservice.dto.UserResponse;
import lk.serenetravels.userservice.entity.UserType;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;
import java.util.UUID;

public interface UserService {

    UserResponse createUser(CreateUserRequest request, boolean temporaryPassword, boolean verified);

    UserResponse registerAgent(RegisterUserRequest request);

    UserResponse verifyUser(UUID id);

    UserResponse getById(UUID id);

    UserResponse getByKeycloakUserId(String keycloakUserId);

    Page<UserResponse> list(UserType userType, Boolean verified, Boolean active, UUID companyId, Pageable pageable);

    UserResponse updateProfile(UUID id, UpdateUserProfileRequest request);

    UserResponse assignManager(UUID agentId, UUID managerId);

    List<UserResponse> getTeam(UUID managerId);

    UserResponse assignCompany(UUID agentId, UUID companyId);

    UserResponse deactivate(UUID id);

    UserResponse reactivate(UUID id);

    UserResponse changeUserType(UUID id, UserType newType);

    // Re-syncs Keycloak's enabled flag for every already-verified, active AGENT
    // at the given company — called after an admin verifies that company, since
    // those agents may have been waiting on exactly that.
    void refreshKeycloakEnabledForCompany(UUID companyId);
}
