package br.com.techne.lyceum.academic.repository;

import br.com.techne.lyceum.academic.domain.Student;
import br.com.techne.lyceum.academic.shared.exception.ResourceNotFoundException;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface StudentRepository extends JpaRepository<Student, Long> {

    Optional<Student> findByEmail(String email);

    Optional<Student> findByPublicId(UUID publicId);

    boolean existsByEmail(String email);

    default Student getByPublicIdOrThrow(UUID publicId) {
        return findByPublicId(publicId)
                .orElseThrow(
                        () ->
                                new ResourceNotFoundException(
                                        "STUDENT_NOT_FOUND",
                                        "Student not found for publicId: " + publicId));
    }
}
