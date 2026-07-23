package br.com.techne.lyceum.academic.repository;

import br.com.techne.lyceum.academic.domain.ClassGroup;
import br.com.techne.lyceum.academic.shared.exception.ResourceNotFoundException;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

@Repository
public interface ClassGroupRepository
        extends JpaRepository<ClassGroup, Long>, JpaSpecificationExecutor<ClassGroup> {

    @Override
    @EntityGraph(attributePaths = {"subject", "subject.course"})
    List<ClassGroup> findAll();

    @Override
    @EntityGraph(attributePaths = {"subject", "subject.course"})
    Page<ClassGroup> findAll(Pageable pageable);

    @EntityGraph(attributePaths = {"subject", "subject.course"})
    Optional<ClassGroup> findByPublicId(UUID publicId);

    List<ClassGroup> findBySubjectId(Long subjectId);

    @EntityGraph(attributePaths = {"subject", "subject.course"})
    Page<ClassGroup> findBySubjectId(Long subjectId, Pageable pageable);

    boolean existsBySubjectId(Long subjectId);

    default ClassGroup getByPublicIdOrThrow(UUID publicId) {
        return findByPublicId(publicId)
                .orElseThrow(
                        () ->
                                new ResourceNotFoundException(
                                        "CLASS_GROUP_NOT_FOUND",
                                        "Class group not found for publicId: " + publicId));
    }
}
