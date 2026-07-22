package br.com.techne.lyceum.academic.repository;

import br.com.techne.lyceum.academic.domain.User;
import br.com.techne.lyceum.academic.shared.exception.ResourceNotFoundException;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {

    Optional<User> findByEmail(String email);

    Optional<User> findByPublicId(UUID publicId);

    boolean existsByEmail(String email);

    default User getByPublicIdOrThrow(UUID publicId) {
        return findByPublicId(publicId)
                .orElseThrow(
                        () ->
                                new ResourceNotFoundException(
                                        "USER_NOT_FOUND",
                                        "User not found for publicId: " + publicId));
    }
}
