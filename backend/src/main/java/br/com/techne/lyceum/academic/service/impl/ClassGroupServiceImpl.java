package br.com.techne.lyceum.academic.service.impl;

import br.com.techne.lyceum.academic.domain.ClassGroup;
import br.com.techne.lyceum.academic.domain.Subject;
import br.com.techne.lyceum.academic.dto.ClassGroupDTO;
import br.com.techne.lyceum.academic.dto.CreateClassGroupRequest;
import br.com.techne.lyceum.academic.dto.UpdateClassGroupRequest;
import br.com.techne.lyceum.academic.repository.ClassGroupRepository;
import br.com.techne.lyceum.academic.repository.SubjectRepository;
import br.com.techne.lyceum.academic.service.ClassGroupService;
import br.com.techne.lyceum.academic.shared.exception.BadRequestException;
import br.com.techne.lyceum.academic.shared.exception.ResourceNotFoundException;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class ClassGroupServiceImpl implements ClassGroupService {

    private final ClassGroupRepository classGroupRepository;
    private final SubjectRepository subjectRepository;

    @Override
    @Transactional(readOnly = true)
    public List<ClassGroupDTO> getClassGroups() {
        return classGroupRepository.findAll().stream().map(this::toDto).toList();
    }

    @Override
    @Transactional(readOnly = true)
    public ClassGroupDTO getClassGroupByPublicId(UUID publicId) {
        return toDto(findClassGroupByPublicId(publicId));
    }

    @Override
    @Transactional
    public ClassGroupDTO createClassGroup(CreateClassGroupRequest request) {
        Subject subject = findSubjectByPublicId(request.subjectPublicId());

        ClassGroup classGroup = new ClassGroup();
        classGroup.setName(request.name());
        classGroup.setDescription(request.description());
        classGroup.setSubject(subject);
        classGroup.setEnrolledStudents(0);
        classGroup.setVacancyLimit(request.vacancyLimit());
        classGroup.setOpenForEnrollment(request.openForEnrollment());

        return toDto(classGroupRepository.save(classGroup));
    }

    @Override
    @Transactional
    public ClassGroupDTO updateClassGroup(UUID publicId, UpdateClassGroupRequest request) {
        ClassGroup classGroup = findClassGroupByPublicId(publicId);

        if (request.name() != null) {
            classGroup.setName(request.name());
        }
        if (request.description() != null) {
            classGroup.setDescription(request.description());
        }
        if (request.subjectPublicId() != null) {
            classGroup.setSubject(findSubjectByPublicId(request.subjectPublicId()));
        }
        if (request.vacancyLimit() != null) {
            if (request.vacancyLimit() < classGroup.getEnrolledStudents()) {
                throw new BadRequestException(
                        "VACANCY_LIMIT_BELOW_ENROLLED",
                        "Vacancy limit cannot be lower than enrolled students ("
                                + classGroup.getEnrolledStudents()
                                + ")");
            }
            classGroup.setVacancyLimit(request.vacancyLimit());
        }
        if (request.enrolledStudents() != null) {
            if (request.enrolledStudents() > classGroup.getVacancyLimit()) {
                throw new BadRequestException(
                        "ENROLLED_STUDENTS_GREATER_THAN_VACANCY_LIMIT",
                        "Enrolled students cannot be greater than vacancy limit");
            }
            classGroup.setEnrolledStudents(request.enrolledStudents());
        }
        if (request.openForEnrollment() != null) {
            classGroup.setOpenForEnrollment(request.openForEnrollment());
        }

        return toDto(classGroupRepository.save(classGroup));
    }

    @Override
    @Transactional
    public void deleteClassGroup(UUID publicId) {
        ClassGroup classGroup = findClassGroupByPublicId(publicId);
        classGroup.markAsDeleted();
        classGroupRepository.save(classGroup);
    }

    private ClassGroup findClassGroupByPublicId(UUID publicId) {
        return classGroupRepository
                .findByPublicId(publicId)
                .orElseThrow(
                        () ->
                                new ResourceNotFoundException(
                                        "CLASS_GROUP_NOT_FOUND",
                                        "Class group not found for publicId: " + publicId));
    }

    private Subject findSubjectByPublicId(UUID publicId) {
        return subjectRepository
                .findByPublicId(publicId)
                .orElseThrow(
                        () ->
                                new ResourceNotFoundException(
                                        "SUBJECT_NOT_FOUND",
                                        "Subject not found for publicId: " + publicId));
    }

    private ClassGroupDTO toDto(ClassGroup classGroup) {
        return new ClassGroupDTO(
                classGroup.getPublicId(),
                classGroup.getName(),
                classGroup.getDescription(),
                classGroup.getSubject().getPublicId(),
                classGroup.getEnrolledStudents(),
                classGroup.getVacancyLimit(),
                classGroup.getOpenForEnrollment());
    }
}
