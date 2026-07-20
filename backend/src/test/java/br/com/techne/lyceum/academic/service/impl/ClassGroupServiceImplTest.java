package br.com.techne.lyceum.academic.service.impl;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import br.com.techne.lyceum.academic.domain.ClassGroup;
import br.com.techne.lyceum.academic.domain.Course;
import br.com.techne.lyceum.academic.domain.Subject;
import br.com.techne.lyceum.academic.dto.ClassGroupDTO;
import br.com.techne.lyceum.academic.dto.CreateClassGroupRequest;
import br.com.techne.lyceum.academic.dto.UpdateClassGroupRequest;
import br.com.techne.lyceum.academic.repository.ClassGroupRepository;
import br.com.techne.lyceum.academic.repository.SubjectRepository;
import br.com.techne.lyceum.academic.shared.exception.BadRequestException;
import br.com.techne.lyceum.academic.shared.exception.ResourceNotFoundException;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class ClassGroupServiceImplTest {

    @Mock private ClassGroupRepository classGroupRepository;

    @Mock private SubjectRepository subjectRepository;

    @InjectMocks private ClassGroupServiceImpl classGroupService;

    private Subject mockedSubject() {
        Course course = new Course();
        course.setId(1L);
        course.setPublicId(UUID.randomUUID());
        course.setName("Computer Science");
        course.setActive(true);

        Subject subject = new Subject();
        subject.setId(1L);
        subject.setPublicId(UUID.randomUUID());
        subject.setName("Algorithms");
        subject.setDescription("Intro");
        subject.setCourse(course);
        return subject;
    }

    private ClassGroup mockedClassGroup(Subject subject) {
        return mockedClassGroup(1L, "Group A", "Morning class", subject, 0, 40, true);
    }

    private ClassGroup mockedClassGroup(
            Long id,
            String name,
            String description,
            Subject subject,
            Integer enrolledStudents,
            Integer vacancyLimit,
            Boolean openForEnrollment) {
        ClassGroup classGroup = new ClassGroup();
        classGroup.setId(id);
        classGroup.setPublicId(UUID.randomUUID());
        classGroup.setName(name);
        classGroup.setDescription(description);
        classGroup.setSubject(subject);
        classGroup.setEnrolledStudents(enrolledStudents);
        classGroup.setVacancyLimit(vacancyLimit);
        classGroup.setOpenForEnrollment(openForEnrollment);
        return classGroup;
    }

    @Test
    void getClassGroups_whenClassGroupsExist_returnsMappedDtos() {
        Subject subject = mockedSubject();
        ClassGroup group1 = mockedClassGroup(1L, "Group A", "Morning", subject, 0, 40, true);
        ClassGroup group2 = mockedClassGroup(2L, "Group B", "Evening", subject, 5, 30, false);
        when(classGroupRepository.findAll()).thenReturn(List.of(group1, group2));

        List<ClassGroupDTO> result = classGroupService.getClassGroups();

        assertEquals(2, result.size());
        assertEquals(group1.getPublicId(), result.get(0).publicId());
        assertEquals("Group A", result.get(0).name());
        assertEquals(subject.getPublicId(), result.get(0).subjectPublicId());
        assertEquals(0, result.get(0).enrolledStudents());
        assertEquals(40, result.get(0).vacancyLimit());
        assertEquals(true, result.get(0).openForEnrollment());
        assertEquals("Group B", result.get(1).name());
        assertEquals(5, result.get(1).enrolledStudents());
        assertEquals(false, result.get(1).openForEnrollment());
        verify(classGroupRepository).findAll();
    }

    @Test
    void getClassGroups_whenEmpty_returnsEmptyList() {
        when(classGroupRepository.findAll()).thenReturn(List.of());

        List<ClassGroupDTO> result = classGroupService.getClassGroups();

        assertTrue(result.isEmpty());
        verify(classGroupRepository).findAll();
    }

    @Test
    void getClassGroupByPublicId_whenExists_returnsMappedDto() {
        Subject subject = mockedSubject();
        ClassGroup classGroup = mockedClassGroup(subject);
        when(classGroupRepository.findByPublicId(classGroup.getPublicId()))
                .thenReturn(Optional.of(classGroup));

        ClassGroupDTO result = classGroupService.getClassGroupByPublicId(classGroup.getPublicId());

        assertEquals(classGroup.getPublicId(), result.publicId());
        assertEquals("Group A", result.name());
        assertEquals(subject.getPublicId(), result.subjectPublicId());
        assertEquals(40, result.vacancyLimit());
    }

    @Test
    void getClassGroupByPublicId_whenMissing_throwsNotFound() {
        UUID publicId = UUID.randomUUID();
        when(classGroupRepository.findByPublicId(publicId)).thenReturn(Optional.empty());

        ResourceNotFoundException ex =
                assertThrows(
                        ResourceNotFoundException.class,
                        () -> classGroupService.getClassGroupByPublicId(publicId));

        assertEquals("CLASS_GROUP_NOT_FOUND", ex.getCode());
    }

    @Test
    void createClassGroup_whenValid_persistsWithZeroEnrolled() {
        Subject subject = mockedSubject();
        ClassGroup classGroup = mockedClassGroup(subject);
        CreateClassGroupRequest request =
                new CreateClassGroupRequest(
                        "Group A", "Morning class", subject.getPublicId(), 40, true);
        when(subjectRepository.findByPublicId(subject.getPublicId()))
                .thenReturn(Optional.of(subject));
        when(classGroupRepository.save(any(ClassGroup.class))).thenReturn(classGroup);

        ClassGroupDTO result = classGroupService.createClassGroup(request);

        assertEquals("Group A", result.name());
        assertEquals(0, result.enrolledStudents());
        assertEquals(40, result.vacancyLimit());
        assertEquals(true, result.openForEnrollment());
        assertEquals(subject.getPublicId(), result.subjectPublicId());
        verify(classGroupRepository).save(any(ClassGroup.class));
    }

    @Test
    void createClassGroup_whenSubjectMissing_throwsNotFound() {
        UUID subjectPublicId = UUID.randomUUID();
        CreateClassGroupRequest request =
                new CreateClassGroupRequest("Group A", "Morning", subjectPublicId, 40, true);
        when(subjectRepository.findByPublicId(subjectPublicId)).thenReturn(Optional.empty());

        ResourceNotFoundException ex =
                assertThrows(
                        ResourceNotFoundException.class,
                        () -> classGroupService.createClassGroup(request));

        assertEquals("SUBJECT_NOT_FOUND", ex.getCode());
        verify(classGroupRepository, never()).save(any());
    }

    @Test
    void updateClassGroup_whenAllFieldsProvided_updatesAndReturnsDto() {
        Subject subject = mockedSubject();
        Subject newSubject = mockedSubject();
        newSubject.setId(2L);
        newSubject.setName("Databases");
        ClassGroup classGroup = mockedClassGroup(subject);
        UpdateClassGroupRequest request =
                new UpdateClassGroupRequest(
                        "Group C", "Night class", newSubject.getPublicId(), 50, false);
        when(classGroupRepository.findByPublicId(classGroup.getPublicId()))
                .thenReturn(Optional.of(classGroup));
        when(subjectRepository.findByPublicId(newSubject.getPublicId()))
                .thenReturn(Optional.of(newSubject));
        when(classGroupRepository.save(any(ClassGroup.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        ClassGroupDTO result =
                classGroupService.updateClassGroup(classGroup.getPublicId(), request);

        assertEquals("Group C", result.name());
        assertEquals("Night class", result.description());
        assertEquals(newSubject.getPublicId(), result.subjectPublicId());
        assertEquals(50, result.vacancyLimit());
        assertEquals(false, result.openForEnrollment());
    }

    @Test
    void updateClassGroup_whenPartialFields_updatesOnlyProvidedFields() {
        Subject subject = mockedSubject();
        ClassGroup classGroup = mockedClassGroup(subject);
        UpdateClassGroupRequest request =
                new UpdateClassGroupRequest(null, null, null, null, false);
        when(classGroupRepository.findByPublicId(classGroup.getPublicId()))
                .thenReturn(Optional.of(classGroup));
        when(classGroupRepository.save(any(ClassGroup.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        ClassGroupDTO result =
                classGroupService.updateClassGroup(classGroup.getPublicId(), request);

        assertEquals("Group A", result.name());
        assertEquals("Morning class", result.description());
        assertEquals(subject.getPublicId(), result.subjectPublicId());
        assertEquals(40, result.vacancyLimit());
        assertEquals(false, result.openForEnrollment());
        verify(subjectRepository, never()).findByPublicId(any());
    }

    @Test
    void updateClassGroup_whenVacancyLimitBelowEnrolled_throwsBadRequest() {
        Subject subject = mockedSubject();
        ClassGroup classGroup = mockedClassGroup(1L, "Group A", "Morning", subject, 20, 40, true);
        UpdateClassGroupRequest request = new UpdateClassGroupRequest(null, null, null, 10, null);
        when(classGroupRepository.findByPublicId(classGroup.getPublicId()))
                .thenReturn(Optional.of(classGroup));

        BadRequestException ex =
                assertThrows(
                        BadRequestException.class,
                        () ->
                                classGroupService.updateClassGroup(
                                        classGroup.getPublicId(), request));

        assertEquals("VACANCY_LIMIT_BELOW_ENROLLED", ex.getCode());
        verify(classGroupRepository, never()).save(any());
    }

    @Test
    void updateClassGroup_whenMissing_throwsNotFound() {
        UUID publicId = UUID.randomUUID();
        UpdateClassGroupRequest request =
                new UpdateClassGroupRequest("Name", "Desc", UUID.randomUUID(), 40, true);
        when(classGroupRepository.findByPublicId(publicId)).thenReturn(Optional.empty());

        ResourceNotFoundException ex =
                assertThrows(
                        ResourceNotFoundException.class,
                        () -> classGroupService.updateClassGroup(publicId, request));

        assertEquals("CLASS_GROUP_NOT_FOUND", ex.getCode());
        verify(classGroupRepository, never()).save(any());
    }

    @Test
    void deleteClassGroup_whenExists_softDeletes() {
        Subject subject = mockedSubject();
        ClassGroup classGroup = mockedClassGroup(subject);
        when(classGroupRepository.findByPublicId(classGroup.getPublicId()))
                .thenReturn(Optional.of(classGroup));
        when(classGroupRepository.save(any(ClassGroup.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        classGroupService.deleteClassGroup(classGroup.getPublicId());

        ArgumentCaptor<ClassGroup> captor = ArgumentCaptor.forClass(ClassGroup.class);
        verify(classGroupRepository).save(captor.capture());
        verify(classGroupRepository, never()).delete(any());
        assertNotNull(captor.getValue().getDeletedAt());
    }

    @Test
    void deleteClassGroup_whenMissing_throwsNotFound() {
        UUID publicId = UUID.randomUUID();
        when(classGroupRepository.findByPublicId(publicId)).thenReturn(Optional.empty());

        ResourceNotFoundException ex =
                assertThrows(
                        ResourceNotFoundException.class,
                        () -> classGroupService.deleteClassGroup(publicId));

        assertEquals("CLASS_GROUP_NOT_FOUND", ex.getCode());
        verify(classGroupRepository, never()).save(any());
        verify(classGroupRepository, never()).delete(any());
    }
}
