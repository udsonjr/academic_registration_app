package br.com.techne.lyceum.academic.repository;

import br.com.techne.lyceum.academic.domain.Course;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface CourseRepository extends JpaRepository<Course, Long> {

    Optional<Course> findByPublicId(UUID publicId);

    boolean existsByPublicId(UUID publicId);
}
