package br.com.techne.lyceum.academic.repository;

import br.com.techne.lyceum.academic.domain.Subject;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface SubjectRepository extends JpaRepository<Subject, Long> {

    Optional<Subject> findByPublicId(UUID publicId);

    List<Subject> findByCourseId(Long courseId);

    boolean existsByCourseId(Long courseId);
}
