package br.com.techne.lyceum.academic.service.impl;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import br.com.techne.lyceum.academic.domain.Student;
import br.com.techne.lyceum.academic.dto.CreateStudentRequest;
import br.com.techne.lyceum.academic.dto.StudentDTO;
import br.com.techne.lyceum.academic.repository.StudentRepository;
import br.com.techne.lyceum.academic.shared.exception.BadRequestException;
import br.com.techne.lyceum.academic.shared.exception.ConflictException;
import br.com.techne.lyceum.academic.shared.exception.ResourceNotFoundException;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

@ExtendWith(MockitoExtension.class)
class StudentServiceImplTest {

    @Mock private StudentRepository studentRepository;

    @Mock private PasswordEncoder passwordEncoder;

    @InjectMocks private StudentServiceImpl studentService;

    private Student mockedStudent() {
        return mockedStudent(1L, "student1", "student1@example.com");
    }

    private Student mockedStudent(Long id, String name, String email) {
        Student student = new Student();
        student.setId(id);
        student.setPublicId(UUID.randomUUID());
        student.setName(name);
        student.setEmail(email);
        student.setPassword("secret1");
        return student;
    }

    @Test
    void getStudents_whenStudentsExist_returnsMappedDtos() {
        Student student1 = mockedStudent(1L, "student1", "student1@example.com");
        Student student2 = mockedStudent(2L, "student2", "student2@example.com");
        when(studentRepository.findAll()).thenReturn(List.of(student1, student2));

        List<StudentDTO> result = studentService.getStudents();

        assertEquals(2, result.size());
        assertEquals(student1.getPublicId(), result.get(0).publicId());
        assertEquals("student1", result.get(0).name());
        assertEquals("student1@example.com", result.get(0).email());
        assertEquals(student2.getPublicId(), result.get(1).publicId());
        assertEquals("student2", result.get(1).name());
        assertEquals("student2@example.com", result.get(1).email());
        verify(studentRepository).findAll();
    }

    @Test
    void getStudents_whenEmpty_returnsEmptyList() {
        when(studentRepository.findAll()).thenReturn(List.of());

        List<StudentDTO> result = studentService.getStudents();

        assertTrue(result.isEmpty());
        verify(studentRepository).findAll();
    }

    @Test
    void getStudentByPublicId_whenExists_returnsMappedDto() {
        when(studentRepository.findByPublicId(any(UUID.class)))
                .thenReturn(Optional.of(mockedStudent()));

        UUID publicId = UUID.randomUUID();
        StudentDTO result = studentService.getStudentByPublicId(publicId);

        assertEquals("student1", result.name());
        assertEquals("student1@example.com", result.email());
        verify(studentRepository).findByPublicId(publicId);
    }

    @Test
    void createStudent_whenEmailAlreadyExists_throwsConflict() {
        CreateStudentRequest request =
                new CreateStudentRequest("student1", "student1@example.com", "secret1", "secret1");
        when(studentRepository.existsByEmail(request.email())).thenReturn(true);

        ConflictException ex =
                assertThrows(ConflictException.class, () -> studentService.createStudent(request));

        assertEquals("EMAIL_ALREADY_REGISTERED", ex.getCode());
        verify(studentRepository, never()).save(any());
    }

    @Test
    void createStudent_whenPasswordsDoNotMatch_throwsBadRequest() {
        CreateStudentRequest request =
                new CreateStudentRequest("student1", "student1@example.com", "secret1", "secret2");
        when(studentRepository.existsByEmail(request.email())).thenReturn(false);

        BadRequestException ex =
                assertThrows(
                        BadRequestException.class, () -> studentService.createStudent(request));

        assertEquals("PASSWORD_MISMATCH", ex.getCode());
        verify(studentRepository, never()).save(any());
    }

    @Test
    void createStudent_whenValid_persistsEncodedPassword() {
        CreateStudentRequest request =
                new CreateStudentRequest("student1", "student1@example.com", "secret1", "secret1");
        when(studentRepository.existsByEmail(request.email())).thenReturn(false);
        when(studentRepository.save(any(Student.class))).thenReturn(mockedStudent());

        StudentDTO result = studentService.createStudent(request);

        assertEquals("student1", result.name());
        assertEquals("student1@example.com", result.email());
        verify(studentRepository).save(any(Student.class));
    }

    @Test
    void getStudentByPublicId_whenMissing_throwsNotFound() {
        UUID publicId = UUID.randomUUID();
        when(studentRepository.findByPublicId(publicId)).thenReturn(Optional.empty());

        ResourceNotFoundException ex =
                assertThrows(
                        ResourceNotFoundException.class,
                        () -> studentService.getStudentByPublicId(publicId));

        assertEquals("STUDENT_NOT_FOUND", ex.getCode());
    }
}
