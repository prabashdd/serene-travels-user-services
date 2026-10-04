package lk.serenetravels.userservice.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import lk.serenetravels.userservice.entity.UserType;

public class UpdateUserTypeRequest {

    @Schema(description = "New user type; the Keycloak realm role is updated to match")
    @NotNull
    private UserType userType;

    public UserType getUserType() {
        return userType;
    }

    public void setUserType(UserType userType) {
        this.userType = userType;
    }
}
