package br.com.techne.lyceum.academic.dto;

import java.util.UUID;

public record ClassGroupDTO(
        UUID publicId,
        String name,
        String description,
        UUID subjectPublicId,
        Integer enrolledStudents,
        Integer vacancyLimit,
        Boolean openForEnrollment) {}
