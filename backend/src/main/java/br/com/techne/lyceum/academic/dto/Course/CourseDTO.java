package br.com.techne.lyceum.academic.dto;

import java.util.UUID;

public record CourseDTO(UUID publicId, String name, String description, Boolean active) {}
