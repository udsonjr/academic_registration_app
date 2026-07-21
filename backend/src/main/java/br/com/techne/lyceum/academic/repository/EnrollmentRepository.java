package br.com.techne.lyceum.academic.repository;

import br.com.techne.lyceum.academic.domain.Enrollment;
import br.com.techne.lyceum.academic.domain.EnrollmentStatus;
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
}
