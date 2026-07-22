package br.com.techne.lyceum.academic.service.impl;

import br.com.techne.lyceum.academic.domain.ClassGroup;
import br.com.techne.lyceum.academic.domain.Subject;
import br.com.techne.lyceum.academic.dto.ClassGroupDTO;
import br.com.techne.lyceum.academic.dto.CreateClassGroupRequest;
import br.com.techne.lyceum.academic.dto.PageResponse;
import br.com.techne.lyceum.academic.dto.UpdateClassGroupRequest;
import br.com.techne.lyceum.academic.repository.ClassGroupRepository;
import br.com.techne.lyceum.academic.repository.SubjectRepository;
import br.com.techne.lyceum.academic.repository.spec.ClassGroupSpecs;
import br.com.techne.lyceum.academic.service.ClassGroupService;
import br.com.techne.lyceum.academic.shared.exception.BadRequestException;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class ClassGroupServiceImpl implements ClassGroupService {

    private final ClassGroupRepository classGroupRepository;
    private final SubjectRepository subjectRepository;

    @Override
    @Transactional(readOnly = true)
    public PageResponse<ClassGroupDTO> getClassGroups(
            String name,
            Boolean openForEnrollment,
            UUID subjectPublicId,
            UUID coursePublicId,
            Pageable pageable) {
        return PageResponse.from(
                classGroupRepository.findAll(
                        ClassGroupSpecs.withFilters(
                                name, openForEnrollment, subjectPublicId, coursePublicId),
                        pageable),
                ClassGroupDTO::from);
    }

    @Override
    @Transactional(readOnly = true)
    public ClassGroupDTO getClassGroupByPublicId(UUID publicId) {
        return ClassGroupDTO.from(classGroupRepository.getByPublicIdOrThrow(publicId));
    }

    @Override
    @Transactional
    public ClassGroupDTO createClassGroup(CreateClassGroupRequest request) {
        Subject subject = subjectRepository.getByPublicIdOrThrow(request.subjectPublicId());

        ClassGroup classGroup = new ClassGroup();
        classGroup.setName(request.name());
        classGroup.setDescription(request.description());
        classGroup.setSubject(subject);
        classGroup.setEnrolledStudents(0);
        classGroup.setVacancyLimit(request.vacancyLimit());
        classGroup.setOpenForEnrollment(request.openForEnrollment());

        return ClassGroupDTO.from(classGroupRepository.save(classGroup));
    }

    @Override
    @Transactional
    public ClassGroupDTO updateClassGroup(UUID publicId, UpdateClassGroupRequest request) {
        ClassGroup classGroup = classGroupRepository.getByPublicIdOrThrow(publicId);

        if (request.name() != null) {
            classGroup.setName(request.name());
        }
        if (request.description() != null) {
            classGroup.setDescription(request.description());
        }
        if (request.subjectPublicId() != null) {
            classGroup.setSubject(
                    subjectRepository.getByPublicIdOrThrow(request.subjectPublicId()));
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
        if (request.openForEnrollment() != null) {
            classGroup.setOpenForEnrollment(request.openForEnrollment());
        }

        return ClassGroupDTO.from(classGroupRepository.save(classGroup));
    }

    @Override
    @Transactional
    public void deleteClassGroup(UUID publicId) {
        ClassGroup classGroup = classGroupRepository.getByPublicIdOrThrow(publicId);
        classGroup.markAsDeleted();
        classGroupRepository.save(classGroup);
    }
}
