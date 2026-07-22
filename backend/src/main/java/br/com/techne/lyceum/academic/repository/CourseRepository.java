package br.com.techne.lyceum.academic.repository;

import br.com.techne.lyceum.academic.domain.Course;
import br.com.techne.lyceum.academic.shared.exception.ResourceNotFoundException;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

@Repository
public interface CourseRepository
        extends JpaRepository<Course, Long>, JpaSpecificationExecutor<Course> {

    Optional<Course> findByPublicId(UUID publicId);

    boolean existsByPublicId(UUID publicId);

    default Course getByPublicIdOrThrow(UUID publicId) {
        return findByPublicId(publicId)
                .orElseThrow(
                        () ->
                                new ResourceNotFoundException(
                                        "COURSE_NOT_FOUND",
                                        "Course not found for publicId: " + publicId));
    }
}
