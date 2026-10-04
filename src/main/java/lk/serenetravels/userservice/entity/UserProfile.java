package lk.serenetravels.userservice.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.PrePersist;
import jakarta.persistence.PreUpdate;
import jakarta.persistence.Table;

import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "user_profile")
public class UserProfile {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(name = "keycloak_user_id", nullable = false, unique = true)
    private String keycloakUserId;

    @Enumerated(EnumType.STRING)
    @Column(name = "user_type", nullable = false)
    private UserType userType;

    // Self-registered users start unverified and need an admin to confirm them;
    // admin-created users are considered verified on creation.
    @Column(name = "verified", nullable = false, columnDefinition = "boolean default false")
    private boolean verified;

    // Independent of verified: tracks ongoing employment/account lifecycle
    // (e.g. an admin disabling someone who has left), not the one-time
    // registration confirmation. Keycloak's enabled flag always mirrors
    // verified && active (see UserServiceImpl.syncKeycloakEnabled).
    @Column(name = "active", nullable = false, columnDefinition = "boolean default true")
    private boolean active = true;

    // Only meaningful for AGENT users; references another UserProfile's id
    // whose userType is MANAGER. No JPA relationship mapping — kept as a
    // plain FK-by-value, consistent with the rest of this entity.
    @Column(name = "manager_id")
    private UUID managerId;

    // Only meaningful for AGENT users: the partner company they work for.
    // ADMIN/MANAGER work directly for the operating company and have no
    // Company record of their own. No JPA relationship mapping, same as
    // managerId above.
    @Column(name = "company_id")
    private UUID companyId;

    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt;

    protected UserProfile() {
    }

    public UserProfile(String keycloakUserId, UserType userType, boolean verified) {
        this.keycloakUserId = keycloakUserId;
        this.userType = userType;
        this.verified = verified;
    }

    @PrePersist
    void onCreate() {
        Instant now = Instant.now();
        this.createdAt = now;
        this.updatedAt = now;
    }

    @PreUpdate
    void onUpdate() {
        this.updatedAt = Instant.now();
    }

    public UUID getId() {
        return id;
    }

    public String getKeycloakUserId() {
        return keycloakUserId;
    }

    public UserType getUserType() {
        return userType;
    }

    public void setUserType(UserType userType) {
        this.userType = userType;
    }

    public boolean isVerified() {
        return verified;
    }

    public void setVerified(boolean verified) {
        this.verified = verified;
    }

    public boolean isActive() {
        return active;
    }

    public void setActive(boolean active) {
        this.active = active;
    }

    public UUID getManagerId() {
        return managerId;
    }

    public void setManagerId(UUID managerId) {
        this.managerId = managerId;
    }

    public UUID getCompanyId() {
        return companyId;
    }

    public void setCompanyId(UUID companyId) {
        this.companyId = companyId;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }

    public Instant getUpdatedAt() {
        return updatedAt;
    }
}
