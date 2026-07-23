package br.com.techne.lyceum.academic.repository.spec;

import br.com.techne.lyceum.academic.domain.Enrollment;
import br.com.techne.lyceum.academic.domain.EnrollmentStatus;
import jakarta.persistence.criteria.JoinType;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import org.springframework.data.jpa.domain.Specification;

public final class EnrollmentSpecs {

    private EnrollmentSpecs() {}

    public static Specification<Enrollment> withFilters(
            EnrollmentStatus status,
            UUID coursePublicId,
            UUID subjectPublicId,
            UUID classGroupPublicId,
            UUID userPublicId) {
        return (root, query, cb) -> {
            if (query != null
                    && query.getResultType() != Long.class
                    && query.getResultType() != long.class) {
                root.fetch("user", JoinType.LEFT);
                var classGroupFetch = root.fetch("classGroup", JoinType.LEFT);
                var subjectFetch = classGroupFetch.fetch("subject", JoinType.LEFT);
                subjectFetch.fetch("course", JoinType.LEFT);
                query.distinct(true);
            }

            List<jakarta.persistence.criteria.Predicate> predicates = new ArrayList<>();

            if (status != null) {
                predicates.add(cb.equal(root.get("status"), status));
            }
            if (userPublicId != null) {
                predicates.add(cb.equal(root.get("user").get("publicId"), userPublicId));
            }
            if (classGroupPublicId != null) {
                predicates.add(
                        cb.equal(root.get("classGroup").get("publicId"), classGroupPublicId));
            }
            if (subjectPublicId != null) {
                predicates.add(
                        cb.equal(
                                root.get("classGroup").get("subject").get("publicId"),
                                subjectPublicId));
            }
            if (coursePublicId != null) {
                predicates.add(
                        cb.equal(
                                root.get("classGroup").get("subject").get("course").get("publicId"),
                                coursePublicId));
            }

            return cb.and(predicates.toArray(jakarta.persistence.criteria.Predicate[]::new));
        };
    }
}
