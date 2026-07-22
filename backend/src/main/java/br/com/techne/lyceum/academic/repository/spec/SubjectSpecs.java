package br.com.techne.lyceum.academic.repository.spec;

import br.com.techne.lyceum.academic.domain.Subject;
import jakarta.persistence.criteria.JoinType;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import org.springframework.data.jpa.domain.Specification;

public final class SubjectSpecs {

    private SubjectSpecs() {}

    public static Specification<Subject> withFilters(String name, UUID coursePublicId) {
        return (root, query, cb) -> {
            if (query != null
                    && query.getResultType() != Long.class
                    && query.getResultType() != long.class) {
                root.fetch("course", JoinType.LEFT);
                query.distinct(true);
            }

            List<jakarta.persistence.criteria.Predicate> predicates = new ArrayList<>();

            if (name != null && !name.isBlank()) {
                predicates.add(
                        cb.like(cb.lower(root.get("name")), "%" + name.trim().toLowerCase() + "%"));
            }
            if (coursePublicId != null) {
                predicates.add(cb.equal(root.get("course").get("publicId"), coursePublicId));
            }

            return cb.and(predicates.toArray(jakarta.persistence.criteria.Predicate[]::new));
        };
    }
}
