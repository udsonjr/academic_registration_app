package br.com.techne.lyceum.academic.dto;

import br.com.techne.lyceum.academic.domain.Student;
import java.util.UUID;

public record StudentDTO(UUID publicId, String name, String email) {

    public static StudentDTO from(Student student) {
        return new StudentDTO(student.getPublicId(), student.getName(), student.getEmail());
    }
}
