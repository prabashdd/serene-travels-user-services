package lk.serenetravels.userservice.dto;

import io.swagger.v3.oas.annotations.media.Schema;

import java.util.UUID;

public class AssignManagerRequest {

    @Schema(description = "Id of the user to assign as manager; must be a MANAGER. Omit/null to clear the assignment")
    private UUID managerId;

    public UUID getManagerId() {
        return managerId;
    }

    public void setManagerId(UUID managerId) {
        this.managerId = managerId;
    }
}
