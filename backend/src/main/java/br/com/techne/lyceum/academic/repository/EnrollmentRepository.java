package br.com.techne.lyceum.academic.repository;

import br.com.techne.lyceum.academic.domain.Enrollment;
import br.com.techne.lyceum.academic.domain.EnrollmentStatus;
import br.com.techne.lyceum.academic.shared.exception.ResourceNotFoundException;
import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface EnrollmentRepository extends JpaRepository<Enrollment, Long> {

    @Override
    @EntityGraph(
            attributePaths = {
                "user",
                "classGroup",
                "classGroup.subject",
                "classGroup.subject.course"
            })
    List<Enrollment> findAll();

    @Override
    @EntityGraph(
            attributePaths = {
                "user",
                "classGroup",
                "classGroup.subject",
                "classGroup.subject.course"
            })
    Page<Enrollment> findAll(Pageable pageable);

    @EntityGraph(
            attributePaths = {
                "user",
                "classGroup",
                "classGroup.subject",
                "classGroup.subject.course"
            })
    Optional<Enrollment> findByPublicId(UUID publicId);

    boolean existsByUserIdAndClassGroupIdAndStatusIn(
            Long userId, Long classGroupId, Collection<EnrollmentStatus> statuses);

    @EntityGraph(
            attributePaths = {
                "user",
                "classGroup",
                "classGroup.subject",
                "classGroup.subject.course"
            })
    List<Enrollment> findAllByUserId(Long userId);

    @EntityGraph(
            attributePaths = {
                "user",
                "classGroup",
                "classGroup.subject",
                "classGroup.subject.course"
            })
    Page<Enrollment> findAllByUserId(Long userId, Pageable pageable);

    @EntityGraph(
            attributePaths = {
                "user",
                "classGroup",
                "classGroup.subject",
                "classGroup.subject.course"
            })
    List<Enrollment> findAllByClassGroupId(Long classGroupId);

    @EntityGraph(
            attributePaths = {
                "user",
                "classGroup",
                "classGroup.subject",
                "classGroup.subject.course"
            })
    Page<Enrollment> findAllByClassGroupId(Long classGroupId, Pageable pageable);

    default Enrollment getByPublicIdOrThrow(UUID publicId) {
        return findByPublicId(publicId)
                .orElseThrow(
                        () ->
                                new ResourceNotFoundException(
                                        "ENROLLMENT_NOT_FOUND",
                                        "Enrollment not found for publicId: " + publicId));
    }
}
