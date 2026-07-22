package br.com.techne.lyceum.academic.service.impl;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import br.com.techne.lyceum.academic.domain.User;
import br.com.techne.lyceum.academic.domain.UserRole;
import br.com.techne.lyceum.academic.dto.AuthResponse;
import br.com.techne.lyceum.academic.dto.LoginRequest;
import br.com.techne.lyceum.academic.dto.RegisterRequest;
import br.com.techne.lyceum.academic.dto.UserDTO;
import br.com.techne.lyceum.academic.repository.UserRepository;
import br.com.techne.lyceum.academic.security.JwtService;
import br.com.techne.lyceum.academic.security.UserPrincipal;
import br.com.techne.lyceum.academic.shared.exception.BadRequestException;
import br.com.techne.lyceum.academic.shared.exception.ConflictException;
import java.util.Base64;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;

@ExtendWith(MockitoExtension.class)
class AuthServiceImplTest {

    @Mock private UserRepository userRepository;
    @Mock private PasswordEncoder passwordEncoder;
    @Mock private AuthenticationManager authenticationManager;

    private JwtService jwtService;
    private AuthServiceImpl authService;

    @BeforeEach
    void setUp() {
        String secret =
                Base64.getEncoder()
                        .encodeToString(
                                "thisisasecretkeyforjwtsigningatleast256bits".getBytes());
        jwtService = new JwtService(secret, 86400000L);
        authService =
                new AuthServiceImpl(
                        userRepository, passwordEncoder, authenticationManager, jwtService);
    }

    @Test
    void register_whenValid_createsStudent() {
        RegisterRequest request =
                new RegisterRequest("student1", "student1@example.com", "secret1", "secret1");
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

        UserDTO result = authService.register(request);

        assertEquals("student1", result.name());
        assertEquals(UserRole.STUDENT, result.role());
        verify(userRepository).save(any(User.class));
    }

    @Test
    void register_whenEmailExists_throwsConflict() {
        RegisterRequest request =
                new RegisterRequest("student1", "student1@example.com", "secret1", "secret1");
        when(userRepository.existsByEmail(request.email())).thenReturn(true);

        ConflictException ex =
                assertThrows(ConflictException.class, () -> authService.register(request));

        assertEquals("EMAIL_ALREADY_REGISTERED", ex.getCode());
        verify(userRepository, never()).save(any());
    }

    @Test
    void register_whenPasswordMismatch_throwsBadRequest() {
        RegisterRequest request =
                new RegisterRequest("student1", "student1@example.com", "secret1", "secret2");
        when(userRepository.existsByEmail(request.email())).thenReturn(false);

        BadRequestException ex =
                assertThrows(BadRequestException.class, () -> authService.register(request));

        assertEquals("PASSWORD_MISMATCH", ex.getCode());
    }

    @Test
    void login_whenValid_returnsBearerToken() {
        User user = new User();
        user.setId(1L);
        user.setPublicId(UUID.randomUUID());
        user.setName("admin");
        user.setEmail("admin@admin");
        user.setPassword("encoded");
        user.setRole(UserRole.ADMIN);

        UserPrincipal principal = new UserPrincipal(user);
        when(authenticationManager.authenticate(any(UsernamePasswordAuthenticationToken.class)))
                .thenReturn(
                        new UsernamePasswordAuthenticationToken(
                                principal, null, principal.getAuthorities()));

        AuthResponse response = authService.login(new LoginRequest("admin@admin", "admin"));

        assertEquals("Bearer", response.tokenType());
        assertEquals(86400L, response.expiresIn());
        assertEquals(UserRole.ADMIN, response.user().role());
        assertEquals("admin", response.user().name());
        assertEquals(user.getPublicId(), jwtService.extractPublicId(response.accessToken()));
    }
}
