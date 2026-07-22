package br.com.techne.lyceum.academic.service;

import br.com.techne.lyceum.academic.domain.UserRole;
import br.com.techne.lyceum.academic.dto.CreateUserRequest;
import br.com.techne.lyceum.academic.dto.PageResponse;
import br.com.techne.lyceum.academic.dto.UpdateUserRequest;
import br.com.techne.lyceum.academic.dto.UserDTO;
import java.util.UUID;
import org.springframework.data.domain.Pageable;

public interface UserService {

    PageResponse<UserDTO> getUsers(Pageable pageable);

    UserDTO getUserByPublicId(UUID publicId);

    UserDTO createUser(CreateUserRequest request);

    /**
     * Creates a user without authorization checks. Callers must enforce access rules (e.g.
     * admin-only or public self-registration as STUDENT).
     */
    UserDTO createUser(
            String name, String email, String password, String confirmPassword, UserRole role);

    UserDTO updateUser(UUID publicId, UpdateUserRequest request);

    void deleteUser(UUID publicId);
}
