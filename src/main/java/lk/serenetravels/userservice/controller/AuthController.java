package lk.serenetravels.userservice.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lk.serenetravels.userservice.dto.ChangePasswordRequest;
import lk.serenetravels.userservice.dto.ForgotPasswordRequest;
import lk.serenetravels.userservice.dto.LoginRequest;
import lk.serenetravels.userservice.dto.RefreshTokenRequest;
import lk.serenetravels.userservice.dto.TokenResponse;
import lk.serenetravels.userservice.service.AuthService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Tag(name = "Auth", description = "Login, token refresh, logout, and password management against Keycloak")
@RestController
@RequestMapping("/api/v1/auth")
public class AuthController {

    private final AuthService authService;

    public AuthController(AuthService authService) {
        this.authService = authService;
    }

    @Operation(summary = "Log in",
            description = "Exchanges a username/password for an access/refresh token pair. No access token "
                    + "required to call this. Fails if the account is unverified or deactivated.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Tokens issued"),
            @ApiResponse(responseCode = "400", description = "Validation failed", content = @Content),
            @ApiResponse(responseCode = "401", description = "Invalid credentials, or account not usable yet "
                    + "(unverified/deactivated/temporary password)", content = @Content)
    })
    @PostMapping("/login")
    public ResponseEntity<TokenResponse> login(@Valid @RequestBody LoginRequest request) {
        return ResponseEntity.ok(authService.login(request.getUsername(), request.getPassword()));
    }

    @Operation(summary = "Refresh an access token",
            description = "Exchanges a refresh token for a new access/refresh token pair. No access token "
                    + "required to call this.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "New tokens issued"),
            @ApiResponse(responseCode = "400", description = "Validation failed", content = @Content),
            @ApiResponse(responseCode = "401", description = "Invalid or expired refresh token", content = @Content)
    })
    @PostMapping("/refresh")
    public ResponseEntity<TokenResponse> refresh(@Valid @RequestBody RefreshTokenRequest request) {
        return ResponseEntity.ok(authService.refresh(request.getRefreshToken()));
    }

    @Operation(summary = "Log out",
            description = "Revokes the given refresh token at Keycloak, ending that session. Idempotent: "
                    + "an already-invalid refresh token still returns success. No access token required to "
                    + "call this.")
    @ApiResponses({
            @ApiResponse(responseCode = "204", description = "Session revoked (or was already invalid)")
    })
    @PostMapping("/logout")
    public ResponseEntity<Void> logout(@Valid @RequestBody RefreshTokenRequest request) {
        authService.logout(request.getRefreshToken());
        return ResponseEntity.noContent().build();
    }

    @Operation(summary = "Change my password",
            description = "Self-service password change for the currently authenticated caller. Validates the "
                    + "current password against Keycloak before applying the new one.")
    @ApiResponses({
            @ApiResponse(responseCode = "204", description = "Password changed"),
            @ApiResponse(responseCode = "400", description = "Validation failed", content = @Content),
            @ApiResponse(responseCode = "401", description = "Current password is incorrect, or missing/invalid "
                    + "token", content = @Content)
    })
    @PatchMapping("/password")
    public ResponseEntity<Void> changePassword(@AuthenticationPrincipal Jwt jwt,
                                                @Valid @RequestBody ChangePasswordRequest request) {
        authService.changePassword(jwt.getSubject(), request.getCurrentPassword(), request.getNewPassword());
        return ResponseEntity.noContent().build();
    }

    @Operation(summary = "Request a password reset email",
            description = "Triggers Keycloak's reset-password email flow for the given username or email. "
                    + "Always returns 202 regardless of whether the account exists or the email could be "
                    + "delivered, so this endpoint can't be used to enumerate accounts. No access token "
                    + "required to call this.")
    @ApiResponses({
            @ApiResponse(responseCode = "202", description = "Request accepted"),
            @ApiResponse(responseCode = "400", description = "Validation failed", content = @Content)
    })
    @PostMapping("/forgot-password")
    public ResponseEntity<Void> forgotPassword(@Valid @RequestBody ForgotPasswordRequest request) {
        authService.forgotPassword(request.getUsernameOrEmail());
        return ResponseEntity.accepted().build();
    }
}
