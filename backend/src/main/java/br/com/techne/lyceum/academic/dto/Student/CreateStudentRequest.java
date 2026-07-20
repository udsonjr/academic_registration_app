package br.com.techne.lyceum.academic.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record CreateStudentRequest(
        @NotBlank @Size(max = 150) String name,
        @NotBlank @Email @Size(max = 255) String email,
        @NotBlank @Size(min = 6, max = 255) String password,
        @NotBlank @Size(min = 6, max = 255) String confirmPassword) {}
