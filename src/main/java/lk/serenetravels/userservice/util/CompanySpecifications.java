package lk.serenetravels.userservice.util;

import lk.serenetravels.userservice.entity.Company;
import org.springframework.data.jpa.domain.Specification;

public final class CompanySpecifications {

    private CompanySpecifications() {
    }

    public static Specification<Company> filter(Boolean verified) {
        return (root, query, builder) -> {
            var predicate = builder.conjunction();
            if (verified != null) {
                predicate = builder.and(predicate, builder.equal(root.get("verified"), verified));
            }
            return predicate;
        };
    }
}
