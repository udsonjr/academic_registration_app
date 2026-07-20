package br.com.techne.lyceum.academic.dto;

import jakarta.validation.constraints.Size;

public record UpdateCourseRequest(
        @Size(min = 1, max = 150) String name,
        @Size(max = 500) String description,
        Boolean active) {}
