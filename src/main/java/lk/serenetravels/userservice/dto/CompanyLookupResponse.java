package lk.serenetravels.userservice.dto;

import io.swagger.v3.oas.annotations.media.Schema;

import java.util.UUID;

@Schema(description = "Minimal company identity for a self-registration selection dropdown")
public class CompanyLookupResponse {

    private final UUID id;
    private final String name;

    public CompanyLookupResponse(UUID id, String name) {
        this.id = id;
        this.name = name;
    }

    public UUID getId() {
        return id;
    }

    public String getName() {
        return name;
    }
}
