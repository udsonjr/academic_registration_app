package br.com.techne.lyceum.academic.service.impl;

import br.com.techne.lyceum.academic.domain.UserRole;
import br.com.techne.lyceum.academic.dto.AuthResponse;
import br.com.techne.lyceum.academic.dto.LoginRequest;
import br.com.techne.lyceum.academic.dto.RegisterRequest;
import br.com.techne.lyceum.academic.dto.UserDTO;
import br.com.techne.lyceum.academic.security.JwtService;
import br.com.techne.lyceum.academic.security.UserPrincipal;
import br.com.techne.lyceum.academic.service.AuthService;
import br.com.techne.lyceum.academic.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class AuthServiceImpl implements AuthService {

    private final UserService userService;
    private final AuthenticationManager authenticationManager;
    private final JwtService jwtService;

    @Override
    @Transactional
    public UserDTO register(RegisterRequest request) {
        return userService.createUser(
                request.name(),
                request.email(),
                request.password(),
                request.confirmPassword(),
                UserRole.STUDENT);
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
