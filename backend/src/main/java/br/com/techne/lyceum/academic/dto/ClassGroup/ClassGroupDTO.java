package br.com.techne.lyceum.academic.dto;

import br.com.techne.lyceum.academic.domain.ClassGroup;
import java.util.UUID;

public record ClassGroupDTO(
        UUID publicId,
        String name,
        String description,
        SubjectDTO subject,
        Integer enrolledStudents,
        Integer vacancyLimit,
        Boolean openForEnrollment) {

    public static ClassGroupDTO from(ClassGroup classGroup) {
        return new ClassGroupDTO(
                classGroup.getPublicId(),
                classGroup.getName(),
                classGroup.getDescription(),
                SubjectDTO.from(classGroup.getSubject()),
                classGroup.getEnrolledStudents(),
                classGroup.getVacancyLimit(),
                classGroup.getOpenForEnrollment());
    }
}
