package br.com.techne.lyceum.academic.service;

import br.com.techne.lyceum.academic.dto.AuthResponse;
import br.com.techne.lyceum.academic.dto.LoginRequest;
import br.com.techne.lyceum.academic.dto.RegisterRequest;
import br.com.techne.lyceum.academic.dto.UserDTO;

public interface AuthService {

    UserDTO register(RegisterRequest request);

    AuthResponse login(LoginRequest request);
}
