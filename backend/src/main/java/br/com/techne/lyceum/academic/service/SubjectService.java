package br.com.techne.lyceum.academic.service;

import br.com.techne.lyceum.academic.dto.CreateSubjectRequest;
import br.com.techne.lyceum.academic.dto.SubjectDTO;
import br.com.techne.lyceum.academic.dto.UpdateSubjectRequest;
import java.util.List;
import java.util.UUID;

public interface SubjectService {

    List<SubjectDTO> getSubjects();

    SubjectDTO getSubjectByPublicId(UUID publicId);

    SubjectDTO createSubject(CreateSubjectRequest request);

    SubjectDTO updateSubject(UUID publicId, UpdateSubjectRequest request);

    void deleteSubject(UUID publicId);
}
