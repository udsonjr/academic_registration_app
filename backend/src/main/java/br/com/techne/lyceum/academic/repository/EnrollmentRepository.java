package br.com.techne.lyceum.academic.repository;

import br.com.techne.lyceum.academic.domain.Enrollment;
import br.com.techne.lyceum.academic.domain.EnrollmentStatus;
import br.com.techne.lyceum.academic.shared.exception.ResourceNotFoundException;
import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface EnrollmentRepository extends JpaRepository<Enrollment, Long> {

    Optional<Enrollment> findByPublicId(UUID publicId);

    boolean existsByStudentIdAndClassGroupIdAndStatusIn(
            Long studentId, Long classGroupId, Collection<EnrollmentStatus> statuses);

    List<Enrollment> findAllByStudentId(Long studentId);

    List<Enrollment> findAllByClassGroupId(Long classGroupId);

    default Enrollment getByPublicIdOrThrow(UUID publicId) {
        return findByPublicId(publicId)
                .orElseThrow(
                        () ->
                                new ResourceNotFoundException(
                                        "ENROLLMENT_NOT_FOUND",
                                        "Enrollment not found for publicId: " + publicId));
    }
}
