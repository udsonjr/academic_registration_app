package br.com.techne.lyceum.academic.service.impl;

import br.com.techne.lyceum.academic.domain.User;
import br.com.techne.lyceum.academic.domain.UserRole;
import br.com.techne.lyceum.academic.dto.AuthResponse;
import br.com.techne.lyceum.academic.dto.LoginRequest;
import br.com.techne.lyceum.academic.dto.RegisterRequest;
import br.com.techne.lyceum.academic.dto.UserDTO;
import br.com.techne.lyceum.academic.repository.UserRepository;
import br.com.techne.lyceum.academic.security.JwtService;
import br.com.techne.lyceum.academic.security.UserPrincipal;
import br.com.techne.lyceum.academic.service.AuthService;
import br.com.techne.lyceum.academic.shared.exception.BadRequestException;
import br.com.techne.lyceum.academic.shared.exception.ConflictException;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class AuthServiceImpl implements AuthService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final AuthenticationManager authenticationManager;
    private final JwtService jwtService;

    @Override
    @Transactional
    public UserDTO register(RegisterRequest request) {
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
        user.setRole(UserRole.STUDENT);

        return UserDTO.from(userRepository.save(user));
    }

    @Override
    @Transactional(readOnly = true)
    public AuthResponse login(LoginRequest request) {
        Authentication authentication =
                authenticationManager.authenticate(
                        new UsernamePasswordAuthenticationToken(
                                request.email(), request.password()));

        UserPrincipal principal = (UserPrincipal) authentication.getPrincipal();
        String token =
                jwtService.generateToken(
                        principal.getPublicId(), principal.getEmail(), principal.getRole());

        return new AuthResponse(
                token,
                "Bearer",
                jwtService.getExpirationMs() / 1000,
                new UserDTO(
                        principal.getPublicId(),
                        principal.getName(),
                        principal.getEmail(),
                        principal.getRole()));
    }
}
