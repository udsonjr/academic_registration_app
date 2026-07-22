package br.com.techne.lyceum.academic.service.impl;

import br.com.techne.lyceum.academic.domain.Course;
import br.com.techne.lyceum.academic.domain.Subject;
import br.com.techne.lyceum.academic.dto.CreateSubjectRequest;
import br.com.techne.lyceum.academic.dto.PageResponse;
import br.com.techne.lyceum.academic.dto.SubjectDTO;
import br.com.techne.lyceum.academic.dto.UpdateSubjectRequest;
import br.com.techne.lyceum.academic.repository.ClassGroupRepository;
import br.com.techne.lyceum.academic.repository.CourseRepository;
import br.com.techne.lyceum.academic.repository.SubjectRepository;
import br.com.techne.lyceum.academic.service.SubjectService;
import br.com.techne.lyceum.academic.shared.exception.ConflictException;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
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
    public PageResponse<SubjectDTO> getSubjects(UUID coursePublicId, Pageable pageable) {
        if (coursePublicId != null) {
            Course course = courseRepository.getByPublicIdOrThrow(coursePublicId);
            return PageResponse.from(
                    subjectRepository.findByCourseId(course.getId(), pageable), SubjectDTO::from);
        }
        return PageResponse.from(subjectRepository.findAll(pageable), SubjectDTO::from);
    }

    @Override
    @Transactional(readOnly = true)
    public SubjectDTO getSubjectByPublicId(UUID publicId) {
        return SubjectDTO.from(subjectRepository.getByPublicIdOrThrow(publicId));
    }

    @Override
    @Transactional
    public SubjectDTO createSubject(CreateSubjectRequest request) {
        Course course = courseRepository.getByPublicIdOrThrow(request.coursePublicId());

        Subject subject = new Subject();
        subject.setName(request.name());
        subject.setDescription(request.description());
        subject.setCourse(course);

        return SubjectDTO.from(subjectRepository.save(subject));
    }

    @Override
    @Transactional
    public SubjectDTO updateSubject(UUID publicId, UpdateSubjectRequest request) {
        Subject subject = subjectRepository.getByPublicIdOrThrow(publicId);

        if (request.name() != null) {
            subject.setName(request.name());
        }
        if (request.description() != null) {
            subject.setDescription(request.description());
        }
        if (request.coursePublicId() != null) {
            subject.setCourse(courseRepository.getByPublicIdOrThrow(request.coursePublicId()));
        }

        return SubjectDTO.from(subjectRepository.save(subject));
    }

    @Override
    @Transactional
    public void deleteSubject(UUID publicId) {
        Subject subject = subjectRepository.getByPublicIdOrThrow(publicId);

        if (classGroupRepository.existsBySubjectId(subject.getId())) {
            throw new ConflictException(
                    "SUBJECT_HAS_CLASS_GROUPS",
                    "Cannot delete subject with associated class groups. publicId: " + publicId);
        }

        subject.markAsDeleted();
        subjectRepository.save(subject);
    }
}
