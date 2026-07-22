package br.com.techne.lyceum.academic.service.impl;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import br.com.techne.lyceum.academic.domain.User;
import br.com.techne.lyceum.academic.domain.UserRole;
import br.com.techne.lyceum.academic.dto.CreateUserRequest;
import br.com.techne.lyceum.academic.dto.PageResponse;
import br.com.techne.lyceum.academic.dto.UpdateUserRequest;
import br.com.techne.lyceum.academic.dto.UserDTO;
import br.com.techne.lyceum.academic.repository.UserRepository;
import br.com.techne.lyceum.academic.security.UserPrincipal;
import br.com.techne.lyceum.academic.shared.exception.BadRequestException;
import br.com.techne.lyceum.academic.shared.exception.ConflictException;
import br.com.techne.lyceum.academic.shared.exception.ForbiddenException;
import br.com.techne.lyceum.academic.shared.exception.ResourceNotFoundException;
import java.util.List;
import java.util.UUID;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;

@ExtendWith(MockitoExtension.class)
class UserServiceImplTest {

    @Mock private UserRepository userRepository;

    @Mock private PasswordEncoder passwordEncoder;

    @InjectMocks private UserServiceImpl userService;

    @AfterEach
    void clearSecurityContext() {
        SecurityContextHolder.clearContext();
    }

    private User mockedUser() {
        return mockedUser(1L, "student1", "student1@example.com", UserRole.STUDENT);
    }

    private User mockedAdmin() {
        return mockedUser(99L, "admin", "admin@admin", UserRole.ADMIN);
    }

    private User mockedUser(Long id, String name, String email, UserRole role) {
        User user = new User();
        user.setId(id);
        user.setPublicId(UUID.randomUUID());
        user.setName(name);
        user.setEmail(email);
        user.setPassword("secret1");
        user.setRole(role);
        return user;
    }

    private void authenticateAs(User user) {
        UserPrincipal principal = new UserPrincipal(user);
        SecurityContextHolder.getContext()
                .setAuthentication(
                        new UsernamePasswordAuthenticationToken(
                                principal, null, principal.getAuthorities()));
    }

    @Test
    void getUsers_whenAdmin_returnsMappedDtos() {
        authenticateAs(mockedAdmin());
        User user1 = mockedUser(1L, "student1", "student1@example.com", UserRole.STUDENT);
        User user2 = mockedUser(2L, "student2", "student2@example.com", UserRole.STUDENT);
        Pageable pageable = PageRequest.of(0, 10);
        when(userRepository.findAll(pageable))
                .thenReturn(new PageImpl<>(List.of(user1, user2), pageable, 2));

        PageResponse<UserDTO> result = userService.getUsers(pageable);

        assertEquals(2, result.content().size());
        assertEquals(user1.getPublicId(), result.content().get(0).publicId());
        assertEquals(UserRole.STUDENT, result.content().get(0).role());
        verify(userRepository).findAll(pageable);
    }

    @Test
    void getUsers_whenStudent_throwsForbidden() {
        authenticateAs(mockedUser());
        Pageable pageable = PageRequest.of(0, 10);

        ForbiddenException ex =
                assertThrows(ForbiddenException.class, () -> userService.getUsers(pageable));

        assertEquals("ACCESS_DENIED", ex.getCode());
        verify(userRepository, never()).findAll(pageable);
    }

    @Test
    void getUserByPublicId_whenSelf_returnsMappedDto() {
        User user = mockedUser();
        authenticateAs(user);
        when(userRepository.getByPublicIdOrThrow(user.getPublicId())).thenReturn(user);

        UserDTO result = userService.getUserByPublicId(user.getPublicId());

        assertEquals("student1", result.name());
        assertEquals(UserRole.STUDENT, result.role());
    }

    @Test
    void getUserByPublicId_whenOtherStudent_throwsForbidden() {
        User current = mockedUser();
        authenticateAs(current);

        ForbiddenException ex =
                assertThrows(
                        ForbiddenException.class,
                        () -> userService.getUserByPublicId(UUID.randomUUID()));

        assertEquals("ACCESS_DENIED", ex.getCode());
    }

    @Test
    void createUser_whenAdmin_persistsWithRole() {
        authenticateAs(mockedAdmin());
        CreateUserRequest request =
                new CreateUserRequest(
                        "student1", "student1@example.com", "secret1", "secret1", UserRole.STUDENT);
        when(userRepository.existsByEmail(request.email())).thenReturn(false);
        when(passwordEncoder.encode("secret1")).thenReturn("encoded");
        when(userRepository.save(any(User.class)))
                .thenAnswer(
                        invocation -> {
                            User saved = invocation.getArgument(0);
                            saved.setId(1L);
                            saved.setPublicId(UUID.randomUUID());
                            return saved;
                        });

        UserDTO result = userService.createUser(request);

        assertEquals("student1", result.name());
        assertEquals(UserRole.STUDENT, result.role());
        verify(userRepository).save(any(User.class));
    }

    @Test
    void createUser_whenEmailAlreadyExists_throwsConflict() {
        authenticateAs(mockedAdmin());
        CreateUserRequest request =
                new CreateUserRequest(
                        "student1", "student1@example.com", "secret1", "secret1", null);
        when(userRepository.existsByEmail(request.email())).thenReturn(true);

        ConflictException ex =
                assertThrows(ConflictException.class, () -> userService.createUser(request));

        assertEquals("EMAIL_ALREADY_REGISTERED", ex.getCode());
        verify(userRepository, never()).save(any());
    }

    @Test
    void createUser_whenPasswordsDoNotMatch_throwsBadRequest() {
        authenticateAs(mockedAdmin());
        CreateUserRequest request =
                new CreateUserRequest(
                        "student1", "student1@example.com", "secret1", "secret2", null);
        when(userRepository.existsByEmail(request.email())).thenReturn(false);

        BadRequestException ex =
                assertThrows(BadRequestException.class, () -> userService.createUser(request));

        assertEquals("PASSWORD_MISMATCH", ex.getCode());
        verify(userRepository, never()).save(any());
    }

    @Test
    void createUser_whenStudent_throwsForbidden() {
        authenticateAs(mockedUser());
        CreateUserRequest request =
                new CreateUserRequest(
                        "student1", "student1@example.com", "secret1", "secret1", null);

        ForbiddenException ex =
                assertThrows(ForbiddenException.class, () -> userService.createUser(request));

        assertEquals("ACCESS_DENIED", ex.getCode());
    }

    @Test
    void updateUser_whenSelf_updatesAndReturnsDto() {
        User user = mockedUser();
        authenticateAs(user);
        UpdateUserRequest request = new UpdateUserRequest("student2", "student2@example.com");
        when(userRepository.getByPublicIdOrThrow(user.getPublicId())).thenReturn(user);
        when(userRepository.existsByEmail(request.email())).thenReturn(false);
        when(userRepository.save(any(User.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        UserDTO result = userService.updateUser(user.getPublicId(), request);

        assertEquals("student2", result.name());
        assertEquals("student2@example.com", result.email());
    }

    @Test
    void updateUser_whenEmailAlreadyRegistered_throwsConflict() {
        User user = mockedUser();
        authenticateAs(user);
        UpdateUserRequest request = new UpdateUserRequest(null, "taken@example.com");
        when(userRepository.getByPublicIdOrThrow(user.getPublicId())).thenReturn(user);
        when(userRepository.existsByEmail("taken@example.com")).thenReturn(true);

        ConflictException ex =
                assertThrows(
                        ConflictException.class,
                        () -> userService.updateUser(user.getPublicId(), request));

        assertEquals("EMAIL_ALREADY_REGISTERED", ex.getCode());
        verify(userRepository, never()).save(any());
    }

    @Test
    void updateUser_whenMissing_throwsNotFound() {
        User admin = mockedAdmin();
        authenticateAs(admin);
        UUID publicId = UUID.randomUUID();
        UpdateUserRequest request = new UpdateUserRequest("student2", "student2@example.com");
        when(userRepository.getByPublicIdOrThrow(publicId))
                .thenThrow(new ResourceNotFoundException("USER_NOT_FOUND", "User not found"));

        ResourceNotFoundException ex =
                assertThrows(
                        ResourceNotFoundException.class,
                        () -> userService.updateUser(publicId, request));

        assertEquals("USER_NOT_FOUND", ex.getCode());
    }

    @Test
    void deleteUser_whenSelf_softDeletes() {
        User user = mockedUser();
        authenticateAs(user);
        when(userRepository.getByPublicIdOrThrow(user.getPublicId())).thenReturn(user);
        when(userRepository.save(any(User.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        userService.deleteUser(user.getPublicId());

        ArgumentCaptor<User> captor = ArgumentCaptor.forClass(User.class);
        verify(userRepository).save(captor.capture());
        verify(userRepository, never()).delete(any());
        assertNotNull(captor.getValue().getDeletedAt());
    }

    @Test
    void deleteUser_whenOtherStudent_throwsForbidden() {
        authenticateAs(mockedUser());

        ForbiddenException ex =
                assertThrows(
                        ForbiddenException.class, () -> userService.deleteUser(UUID.randomUUID()));

        assertEquals("ACCESS_DENIED", ex.getCode());
    }
}
