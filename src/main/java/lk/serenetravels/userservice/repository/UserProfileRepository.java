package lk.serenetravels.userservice.repository;

import lk.serenetravels.userservice.entity.UserProfile;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface UserProfileRepository extends JpaRepository<UserProfile, UUID>, JpaSpecificationExecutor<UserProfile> {

    Optional<UserProfile> findByKeycloakUserId(String keycloakUserId);

    List<UserProfile> findByManagerId(UUID managerId);
}
