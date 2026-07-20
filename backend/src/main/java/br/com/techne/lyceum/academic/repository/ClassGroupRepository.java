package br.com.techne.lyceum.academic.repository;

import br.com.techne.lyceum.academic.domain.ClassGroup;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ClassGroupRepository extends JpaRepository<ClassGroup, Long> {

    Optional<ClassGroup> findByPublicId(UUID publicId);

    List<ClassGroup> findBySubjectId(Long subjectId);

    boolean existsBySubjectId(Long subjectId);
}
