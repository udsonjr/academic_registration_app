package br.com.techne.lyceum.academic.service.impl;

import br.com.techne.lyceum.academic.domain.Student;
import br.com.techne.lyceum.academic.dto.CreateStudentRequest;
import br.com.techne.lyceum.academic.dto.StudentDTO;
import br.com.techne.lyceum.academic.dto.UpdateStudentRequest;
import br.com.techne.lyceum.academic.repository.StudentRepository;
import br.com.techne.lyceum.academic.service.StudentService;
import br.com.techne.lyceum.academic.shared.exception.BadRequestException;
import br.com.techne.lyceum.academic.shared.exception.ConflictException;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class StudentServiceImpl implements StudentService {

    private final StudentRepository studentRepository;
    private final PasswordEncoder passwordEncoder;

    @Override
    @Transactional(readOnly = true)
    public List<StudentDTO> getStudents() {
        return studentRepository.findAll().stream().map(this::toDto).toList();
    }

    @Override
    @Transactional(readOnly = true)
    public StudentDTO getStudentByPublicId(UUID publicId) {
        return toDto(studentRepository.getByPublicIdOrThrow(publicId));
    }

    @Override
    @Transactional
    public StudentDTO createStudent(CreateStudentRequest request) {
        if (studentRepository.existsByEmail(request.email())) {
            throw new ConflictException(
                    "EMAIL_ALREADY_REGISTERED", "Email already registered: " + request.email());
        }

        if (!request.password().equals(request.confirmPassword())) {
            throw new BadRequestException(
                    "PASSWORD_MISMATCH", "Password and confirm password do not match");
        }

        Student student = new Student();
        student.setName(request.name());
        student.setEmail(request.email());
        student.setPassword(passwordEncoder.encode(request.password()));

        return toDto(studentRepository.save(student));
    }

    @Override
    @Transactional
    public StudentDTO updateStudent(UUID publicId, UpdateStudentRequest request) {
        Student student = studentRepository.getByPublicIdOrThrow(publicId);

        if (request.name() != null) {
            student.setName(request.name());
        }
        if (request.email() != null && !request.email().equals(student.getEmail())) {
            if (studentRepository.existsByEmail(request.email())) {
                throw new ConflictException(
                        "EMAIL_ALREADY_REGISTERED", "Email already registered: " + request.email());
            }
            student.setEmail(request.email());
        }

        return toDto(studentRepository.save(student));
    }

    @Override
    @Transactional
    public void deleteStudent(UUID publicId) {
        Student student = studentRepository.getByPublicIdOrThrow(publicId);
        student.markAsDeleted();
        studentRepository.save(student);
    }

    private StudentDTO toDto(Student student) {
        return new StudentDTO(student.getPublicId(), student.getName(), student.getEmail());
    }
}
