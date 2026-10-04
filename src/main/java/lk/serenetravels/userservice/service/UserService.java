package lk.serenetravels.userservice.service;

import lk.serenetravels.userservice.dto.CreateUserRequest;
import lk.serenetravels.userservice.dto.UpdateUserProfileRequest;
import lk.serenetravels.userservice.dto.UserResponse;
import lk.serenetravels.userservice.entity.UserType;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;
import java.util.UUID;

public interface UserService {

    UserResponse createUser(CreateUserRequest request, boolean temporaryPassword, boolean verified);

    UserResponse verifyUser(UUID id);

    UserResponse getById(UUID id);

    UserResponse getByKeycloakUserId(String keycloakUserId);

    Page<UserResponse> list(UserType userType, Boolean verified, Boolean active, Pageable pageable);

    UserResponse updateProfile(UUID id, UpdateUserProfileRequest request);

    UserResponse assignManager(UUID agentId, UUID managerId);

    List<UserResponse> getTeam(UUID managerId);

    UserResponse deactivate(UUID id);

    UserResponse reactivate(UUID id);

    UserResponse changeUserType(UUID id, UserType newType);
}
