package br.com.techne.lyceum.academic.dto;

import java.util.UUID;

public record SubjectDTO(UUID publicId, String name, String description, UUID coursePublicId) {}
