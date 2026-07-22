package br.com.techne.lyceum.academic.repository;

import br.com.techne.lyceum.academic.domain.Subject;
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
public interface SubjectRepository
        extends JpaRepository<Subject, Long>, JpaSpecificationExecutor<Subject> {

    @Override
    @EntityGraph(attributePaths = {"course"})
    List<Subject> findAll();

    @Override
    @EntityGraph(attributePaths = {"course"})
    Page<Subject> findAll(Pageable pageable);

    @EntityGraph(attributePaths = {"course"})
    Optional<Subject> findByPublicId(UUID publicId);

    List<Subject> findByCourseId(Long courseId);

    @EntityGraph(attributePaths = {"course"})
    Page<Subject> findByCourseId(Long courseId, Pageable pageable);

    boolean existsByCourseId(Long courseId);

    default Subject getByPublicIdOrThrow(UUID publicId) {
        return findByPublicId(publicId)
                .orElseThrow(
                        () ->
                                new ResourceNotFoundException(
                                        "SUBJECT_NOT_FOUND",
                                        "Subject not found for publicId: " + publicId));
    }
}
