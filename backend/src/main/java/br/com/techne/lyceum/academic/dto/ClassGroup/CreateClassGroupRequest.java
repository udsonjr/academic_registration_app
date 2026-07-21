package br.com.techne.lyceum.academic.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.util.UUID;

public record CreateClassGroupRequest(
        @NotBlank @Size(max = 150) String name,
        @Size(max = 500) String description,
        @NotNull UUID subjectPublicId,
        @NotNull @Min(1) Integer vacancyLimit,
        @NotNull Boolean openForEnrollment) {}
