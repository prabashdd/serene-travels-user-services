package lk.serenetravels.userservice.dto;

import io.swagger.v3.oas.annotations.media.Schema;

import java.util.UUID;

public class AssignCompanyRequest {

    @Schema(description = "Id of the company this user works for. Omit/null to clear the assignment")
    private UUID companyId;

    public UUID getCompanyId() {
        return companyId;
    }

    public void setCompanyId(UUID companyId) {
        this.companyId = companyId;
    }
}
