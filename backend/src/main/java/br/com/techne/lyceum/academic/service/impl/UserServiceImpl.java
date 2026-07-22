package br.com.techne.lyceum.academic.service.impl;

import br.com.techne.lyceum.academic.domain.User;
import br.com.techne.lyceum.academic.domain.UserRole;
import br.com.techne.lyceum.academic.dto.CreateUserRequest;
import br.com.techne.lyceum.academic.dto.UpdateUserRequest;
import br.com.techne.lyceum.academic.dto.UserDTO;
import br.com.techne.lyceum.academic.repository.UserRepository;
import br.com.techne.lyceum.academic.security.SecurityUtils;
import br.com.techne.lyceum.academic.service.UserService;
import br.com.techne.lyceum.academic.shared.exception.BadRequestException;
import br.com.techne.lyceum.academic.shared.exception.ConflictException;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
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
    public List<UserDTO> getUsers() {
        SecurityUtils.requireAdmin();
        return userRepository.findAll().stream().map(UserDTO::from).toList();
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

        if (userRepository.existsByEmail(request.email())) {
            throw new ConflictException(
                    "EMAIL_ALREADY_REGISTERED", "Email already registered: " + request.email());
        }

        if (!request.password().equals(request.confirmPassword())) {
            throw new BadRequestException(
                    "PASSWORD_MISMATCH", "Password and confirm password do not match");
        }

        User user = new User();
        user.setName(request.name());
        user.setEmail(request.email());
        user.setPassword(passwordEncoder.encode(request.password()));
        user.setRole(request.role() != null ? request.role() : UserRole.STUDENT);

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
