package br.com.techne.lyceum.academic.service;

import br.com.techne.lyceum.academic.dto.CreateStudentRequest;
import br.com.techne.lyceum.academic.dto.StudentDTO;
import br.com.techne.lyceum.academic.dto.UpdateStudentRequest;
import java.util.List;
import java.util.UUID;

public interface StudentService {

    List<StudentDTO> getStudents();

    StudentDTO getStudentByPublicId(UUID publicId);

    StudentDTO createStudent(CreateStudentRequest request);

    StudentDTO updateStudent(UUID publicId, UpdateStudentRequest request);

    void deleteStudent(UUID publicId);
}
