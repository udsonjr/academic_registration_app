package br.com.techne.lyceum.academic.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.Size;

public record UpdateUserRequest(
        @Size(min = 1, max = 150) String name, @Email @Size(max = 255) String email) {}
