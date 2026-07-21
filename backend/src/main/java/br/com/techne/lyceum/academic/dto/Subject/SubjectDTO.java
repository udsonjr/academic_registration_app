package br.com.techne.lyceum.academic.dto;

import br.com.techne.lyceum.academic.domain.Subject;
import java.util.UUID;

public record SubjectDTO(UUID publicId, String name, String description, CourseDTO course) {

    public static SubjectDTO from(Subject subject) {
        return new SubjectDTO(
                subject.getPublicId(),
                subject.getName(),
                subject.getDescription(),
                CourseDTO.from(subject.getCourse()));
    }
}
