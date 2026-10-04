package lk.serenetravels.userservice.util;

import lk.serenetravels.userservice.entity.UserProfile;
import lk.serenetravels.userservice.entity.UserType;
import org.springframework.data.jpa.domain.Specification;

import java.util.UUID;

public final class UserProfileSpecifications {

    private UserProfileSpecifications() {
    }

    public static Specification<UserProfile> filter(UserType userType, Boolean verified, Boolean active,
            UUID companyId) {
        return (root, query, builder) -> {
            var predicate = builder.conjunction();
            if (userType != null) {
                predicate = builder.and(predicate, builder.equal(root.get("userType"), userType));
            }
            if (verified != null) {
                predicate = builder.and(predicate, builder.equal(root.get("verified"), verified));
            }
            if (active != null) {
                predicate = builder.and(predicate, builder.equal(root.get("active"), active));
            }
            if (companyId != null) {
                predicate = builder.and(predicate, builder.equal(root.get("companyId"), companyId));
            }
            return predicate;
        };
    }
}
