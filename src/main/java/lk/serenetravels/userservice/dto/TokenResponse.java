package lk.serenetravels.userservice.dto;

import io.swagger.v3.oas.annotations.media.Schema;

public class TokenResponse {

    @Schema(description = "JWT bearer token; send as 'Authorization: Bearer <accessToken>'")
    private final String accessToken;

    @Schema(description = "Opaque token used to obtain a new token pair via /refresh, without logging in again")
    private final String refreshToken;

    @Schema(example = "Bearer")
    private final String tokenType;

    @Schema(description = "Access token lifetime, in seconds")
    private final long expiresIn;

    @Schema(description = "Refresh token lifetime, in seconds")
    private final long refreshExpiresIn;

    public TokenResponse(String accessToken, String refreshToken, String tokenType,
                          long expiresIn, long refreshExpiresIn) {
        this.accessToken = accessToken;
        this.refreshToken = refreshToken;
        this.tokenType = tokenType;
        this.expiresIn = expiresIn;
        this.refreshExpiresIn = refreshExpiresIn;
    }

    public String getAccessToken() {
        return accessToken;
    }

    public String getRefreshToken() {
        return refreshToken;
    }

    public String getTokenType() {
        return tokenType;
    }

    public long getExpiresIn() {
        return expiresIn;
    }

    public long getRefreshExpiresIn() {
        return refreshExpiresIn;
    }
}
