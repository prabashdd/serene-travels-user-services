package lk.serenetravels.userservice.service;

import lk.serenetravels.userservice.dto.TokenResponse;

public interface AuthService {

    TokenResponse login(String username, String password);

    TokenResponse refresh(String refreshToken);

    void logout(String refreshToken);

    void changePassword(String keycloakUserId, String currentPassword, String newPassword);

    void forgotPassword(String usernameOrEmail);
}
