package br.com.techne.lyceum.academic.dto;

import br.com.techne.lyceum.academic.domain.Course;
import java.util.UUID;

public record CourseDTO(UUID publicId, String name, String description, Boolean active) {

    public static CourseDTO from(Course course) {
        return new CourseDTO(
                course.getPublicId(),
                course.getName(),
                course.getDescription(),
                course.getActive());
    }
}
