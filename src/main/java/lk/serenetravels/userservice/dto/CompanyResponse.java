package lk.serenetravels.userservice.dto;

import java.time.Instant;
import java.util.UUID;

public class CompanyResponse {

    private final UUID id;
    private final String name;
    private final String address;
    private final String registrationNumber;
    private final String phone;
    private final String email;
    private final boolean verified;
    private final Instant createdAt;
    private final Instant updatedAt;

    public CompanyResponse(UUID id, String name, String address, String registrationNumber,
                            String phone, String email, boolean verified, Instant createdAt, Instant updatedAt) {
        this.id = id;
        this.name = name;
        this.address = address;
        this.registrationNumber = registrationNumber;
        this.phone = phone;
        this.email = email;
        this.verified = verified;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
    }

    public UUID getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public String getAddress() {
        return address;
    }

    public String getRegistrationNumber() {
        return registrationNumber;
    }

    public String getPhone() {
        return phone;
    }

    public String getEmail() {
        return email;
    }

    public boolean isVerified() {
        return verified;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }

    public Instant getUpdatedAt() {
        return updatedAt;
    }
}
