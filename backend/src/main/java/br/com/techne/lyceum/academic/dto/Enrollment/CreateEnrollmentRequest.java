package br.com.techne.lyceum.academic.dto;

import jakarta.validation.constraints.NotNull;
import java.util.UUID;

public record CreateEnrollmentRequest(
        @NotNull UUID studentPublicId, @NotNull UUID classGroupPublicId) {}
