package br.com.techne.lyceum.academic.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.Size;
import java.util.UUID;

public record UpdateClassGroupRequest(
        @Size(min = 1, max = 150) String name,
        @Size(max = 500) String description,
        UUID subjectPublicId,
        @Min(1) Integer vacancyLimit,
        Integer enrolledStudents,
        Boolean openForEnrollment) {}
