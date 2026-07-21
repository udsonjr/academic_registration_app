package br.com.techne.lyceum.academic.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.util.UUID;

public record CreateSubjectRequest(
        @NotBlank @Size(max = 150) String name,
        @Size(max = 500) String description,
        @NotNull UUID coursePublicId) {}
