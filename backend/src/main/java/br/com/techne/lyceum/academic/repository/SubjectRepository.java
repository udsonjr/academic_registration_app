package br.com.techne.lyceum.academic.repository;

import br.com.techne.lyceum.academic.domain.Subject;
import br.com.techne.lyceum.academic.shared.exception.ResourceNotFoundException;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface SubjectRepository extends JpaRepository<Subject, Long> {

    @Override
    @EntityGraph(attributePaths = {"course"})
    List<Subject> findAll();

    @EntityGraph(attributePaths = {"course"})
    Optional<Subject> findByPublicId(UUID publicId);

    List<Subject> findByCourseId(Long courseId);

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
