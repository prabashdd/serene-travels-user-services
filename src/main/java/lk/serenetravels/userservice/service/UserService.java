package lk.serenetravels.userservice.service;

import lk.serenetravels.userservice.dto.CreateUserRequest;
import lk.serenetravels.userservice.dto.UserResponse;

import java.util.UUID;

public interface UserService {

    UserResponse createUser(CreateUserRequest request, boolean temporaryPassword, boolean verified);

    UserResponse verifyUser(UUID id);
}
