package br.com.techne.lyceum.academic.dto;

import java.util.UUID;

public record StudentDTO(
        UUID publicId,
        String name,
        String email
) {
}
