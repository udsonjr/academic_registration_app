package br.com.techne.lyceum.academic.service.impl;

import br.com.techne.lyceum.academic.domain.Course;
import br.com.techne.lyceum.academic.domain.Subject;
import br.com.techne.lyceum.academic.dto.CreateSubjectRequest;
import br.com.techne.lyceum.academic.dto.SubjectDTO;
import br.com.techne.lyceum.academic.dto.UpdateSubjectRequest;
import br.com.techne.lyceum.academic.repository.ClassGroupRepository;
import br.com.techne.lyceum.academic.repository.CourseRepository;
import br.com.techne.lyceum.academic.repository.SubjectRepository;
import br.com.techne.lyceum.academic.service.SubjectService;
import br.com.techne.lyceum.academic.shared.exception.ConflictException;
import br.com.techne.lyceum.academic.shared.exception.ResourceNotFoundException;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class SubjectServiceImpl implements SubjectService {

    private final SubjectRepository subjectRepository;
    private final CourseRepository courseRepository;
    private final ClassGroupRepository classGroupRepository;

    @Override
    @Transactional(readOnly = true)
    public List<SubjectDTO> getSubjects() {
        return subjectRepository.findAll().stream().map(this::toDto).toList();
    }

    @Override
    @Transactional(readOnly = true)
    public SubjectDTO getSubjectByPublicId(UUID publicId) {
        return toDto(findSubjectByPublicId(publicId));
    }

    @Override
    @Transactional
    public SubjectDTO createSubject(CreateSubjectRequest request) {
        Course course = findCourseByPublicId(request.coursePublicId());

        Subject subject = new Subject();
        subject.setName(request.name());
        subject.setDescription(request.description());
        subject.setCourse(course);

        return toDto(subjectRepository.save(subject));
    }

    @Override
    @Transactional
    public SubjectDTO updateSubject(UUID publicId, UpdateSubjectRequest request) {
        Subject subject = findSubjectByPublicId(publicId);

        if (request.name() != null) {
            subject.setName(request.name());
        }
        if (request.description() != null) {
            subject.setDescription(request.description());
        }
        if (request.coursePublicId() != null) {
            subject.setCourse(findCourseByPublicId(request.coursePublicId()));
        }

        return toDto(subjectRepository.save(subject));
    }

    @Override
    @Transactional
    public void deleteSubject(UUID publicId) {
        Subject subject = findSubjectByPublicId(publicId);

        if (classGroupRepository.existsBySubjectId(subject.getId())) {
            throw new ConflictException(
                    "SUBJECT_HAS_CLASS_GROUPS",
                    "Cannot delete subject with associated class groups. publicId: " + publicId);
        }

        subject.markAsDeleted();
        subjectRepository.save(subject);
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

    private Course findCourseByPublicId(UUID publicId) {
        return courseRepository
                .findByPublicId(publicId)
                .orElseThrow(
                        () ->
                                new ResourceNotFoundException(
                                        "COURSE_NOT_FOUND",
                                        "Course not found for publicId: " + publicId));
    }

    private SubjectDTO toDto(Subject subject) {
        return new SubjectDTO(
                subject.getPublicId(),
                subject.getName(),
                subject.getDescription(),
                subject.getCourse().getPublicId());
    }
}
