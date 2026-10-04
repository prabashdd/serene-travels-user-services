package lk.serenetravels.userservice.service.impl;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lk.serenetravels.userservice.config.KeycloakProperties;
import lk.serenetravels.userservice.dto.TokenResponse;
import lk.serenetravels.userservice.exception.AuthenticationFailedException;
import lk.serenetravels.userservice.service.AuthService;
import lk.serenetravels.userservice.service.KeycloakUserService;
import org.keycloak.representations.idm.UserRepresentation;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.MediaType;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.HttpClientErrorException;

import java.util.Optional;

@Service
public class AuthServiceImpl implements AuthService {

    private static final Logger log = LoggerFactory.getLogger(AuthServiceImpl.class);

    private static final String GRANT_TYPE_PASSWORD = "password";
    private static final String GRANT_TYPE_REFRESH = "refresh_token";

    private final RestClient restClient;
    private final KeycloakProperties properties;
    private final KeycloakUserService keycloakUserService;

    public AuthServiceImpl(KeycloakProperties properties, KeycloakUserService keycloakUserService) {
        this.properties = properties;
        this.keycloakUserService = keycloakUserService;
        // Spring's default RestClient picks java.net.http.HttpClient, whose Selector
        // hits JDK-8312215 (loopback pipe connect fails) in this sandboxed environment.
        // SimpleClientHttpRequestFactory uses blocking HttpURLConnection instead, avoiding it.
        this.restClient = RestClient.builder()
                .requestFactory(new SimpleClientHttpRequestFactory())
                .build();
    }

    @Override
    public TokenResponse login(String username, String password) {
        MultiValueMap<String, String> form = baseForm();
        form.add("grant_type", GRANT_TYPE_PASSWORD);
        form.add("username", username);
        form.add("password", password);
        return requestToken(form, "Invalid username or password");
    }

    @Override
    public TokenResponse refresh(String refreshToken) {
        MultiValueMap<String, String> form = baseForm();
        form.add("grant_type", GRANT_TYPE_REFRESH);
        form.add("refresh_token", refreshToken);
        return requestToken(form, "Invalid or expired refresh token");
    }

    @Override
    public void logout(String refreshToken) {
        MultiValueMap<String, String> form = baseForm();
        form.add("refresh_token", refreshToken);
        try {
            restClient.post()
                    .uri(logoutUrl())
                    .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                    .body(form)
                    .retrieve()
                    .toBodilessEntity();
        } catch (HttpClientErrorException ex) {
            // Already-invalid/expired refresh token: the session is effectively
            // over either way, so logout is treated as idempotent.
        }
    }

    @Override
    public void changePassword(String keycloakUserId, String currentPassword, String newPassword) {
        UserRepresentation user = keycloakUserService.getUser(keycloakUserId);
        try {
            login(user.getUsername(), currentPassword);
        } catch (AuthenticationFailedException ex) {
            throw new AuthenticationFailedException("Current password is incorrect");
        }
        keycloakUserService.setPassword(keycloakUserId, newPassword, false);
    }

    @Override
    public void forgotPassword(String usernameOrEmail) {
        Optional<UserRepresentation> user = keycloakUserService.findByUsernameOrEmail(usernameOrEmail);
        // Deliberately silent if not found, so this endpoint can't be used to
        // enumerate which usernames/emails exist in the system. Likewise, a failure
        // to actually deliver the email (e.g. no SMTP server configured in this
        // environment) is an operational concern, not one the caller should see.
        user.ifPresent(u -> {
            try {
                keycloakUserService.sendPasswordResetEmail(u.getId());
            } catch (RuntimeException ex) {
                log.warn("Failed to send password reset email for user {}", u.getId(), ex);
            }
        });
    }

    private String logoutUrl() {
        return properties.getServerUrl() + "/realms/" + properties.getRealm()
                + "/protocol/openid-connect/logout";
    }

    private MultiValueMap<String, String> baseForm() {
        MultiValueMap<String, String> form = new LinkedMultiValueMap<>();
        form.add("client_id", properties.getAdminClientId());
        form.add("client_secret", properties.getAdminClientSecret());
        return form;
    }

    private TokenResponse requestToken(MultiValueMap<String, String> form, String failureMessage) {
        try {
            KeycloakTokenPayload payload = restClient.post()
                    .uri(tokenUrl())
                    .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                    .body(form)
                    .retrieve()
                    .body(KeycloakTokenPayload.class);

            return new TokenResponse(payload.access_token, payload.refresh_token, payload.token_type,
                    payload.expires_in, payload.refresh_expires_in);
        } catch (HttpClientErrorException ex) {
            throw new AuthenticationFailedException(failureMessage);
        }
    }

    private String tokenUrl() {
        return properties.getServerUrl() + "/realms/" + properties.getRealm()
                + "/protocol/openid-connect/token";
    }

    // Maps only the fields we use from Keycloak's token endpoint response;
    // the rest (scope, session_state, etc.) are ignored.
    @JsonIgnoreProperties(ignoreUnknown = true)
    private static class KeycloakTokenPayload {
        public String access_token;
        public String refresh_token;
        public String token_type;
        public long expires_in;
        public long refresh_expires_in;
    }
}
