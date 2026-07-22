package br.com.techne.lyceum.academic.service.impl;

import br.com.techne.lyceum.academic.domain.User;
import br.com.techne.lyceum.academic.domain.UserRole;
import br.com.techne.lyceum.academic.dto.CreateUserRequest;
import br.com.techne.lyceum.academic.dto.PageResponse;
import br.com.techne.lyceum.academic.dto.UpdateUserRequest;
import br.com.techne.lyceum.academic.dto.UserDTO;
import br.com.techne.lyceum.academic.repository.UserRepository;
import br.com.techne.lyceum.academic.security.SecurityUtils;
import br.com.techne.lyceum.academic.service.UserService;
import br.com.techne.lyceum.academic.shared.exception.BadRequestException;
import br.com.techne.lyceum.academic.shared.exception.ConflictException;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class UserServiceImpl implements UserService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    @Override
    @Transactional(readOnly = true)
    public PageResponse<UserDTO> getUsers(Pageable pageable) {
        SecurityUtils.requireAdmin();
        return PageResponse.from(userRepository.findAll(pageable), UserDTO::from);
    }

    @Override
    @Transactional(readOnly = true)
    public UserDTO getUserByPublicId(UUID publicId) {
        SecurityUtils.requireSelfOrAdmin(publicId);
        return UserDTO.from(userRepository.getByPublicIdOrThrow(publicId));
    }

    @Override
    @Transactional
    public UserDTO createUser(CreateUserRequest request) {
        SecurityUtils.requireAdmin();
        return createUser(
                request.name(),
                request.email(),
                request.password(),
                request.confirmPassword(),
                request.role() != null ? request.role() : UserRole.STUDENT);
    }

    @Override
    @Transactional
    public UserDTO createUser(
            String name, String email, String password, String confirmPassword, UserRole role) {
        if (userRepository.existsByEmail(email)) {
            throw new ConflictException(
                    "EMAIL_ALREADY_REGISTERED", "Email already registered: " + email);
        }

        if (!password.equals(confirmPassword)) {
            throw new BadRequestException(
                    "PASSWORD_MISMATCH", "Password and confirm password do not match");
        }

        User user = new User();
        user.setName(name);
        user.setEmail(email);
        user.setPassword(passwordEncoder.encode(password));
        user.setRole(role != null ? role : UserRole.STUDENT);

        return UserDTO.from(userRepository.save(user));
    }

    @Override
    @Transactional
    public UserDTO updateUser(UUID publicId, UpdateUserRequest request) {
        SecurityUtils.requireSelfOrAdmin(publicId);
        User user = userRepository.getByPublicIdOrThrow(publicId);

        if (request.name() != null) {
            user.setName(request.name());
        }
        if (request.email() != null && !request.email().equals(user.getEmail())) {
            if (userRepository.existsByEmail(request.email())) {
                throw new ConflictException(
                        "EMAIL_ALREADY_REGISTERED", "Email already registered: " + request.email());
            }
            user.setEmail(request.email());
        }

        return UserDTO.from(userRepository.save(user));
    }

    @Override
    @Transactional
    public void deleteUser(UUID publicId) {
        SecurityUtils.requireSelfOrAdmin(publicId);
        User user = userRepository.getByPublicIdOrThrow(publicId);
        user.markAsDeleted();
        userRepository.save(user);
    }
}
