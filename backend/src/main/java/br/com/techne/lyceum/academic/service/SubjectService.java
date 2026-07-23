package br.com.techne.lyceum.academic.service;

import br.com.techne.lyceum.academic.dto.CreateSubjectRequest;
import br.com.techne.lyceum.academic.dto.PageResponse;
import br.com.techne.lyceum.academic.dto.SubjectDTO;
import br.com.techne.lyceum.academic.dto.UpdateSubjectRequest;
import java.util.UUID;
import org.springframework.data.domain.Pageable;

public interface SubjectService {

    PageResponse<SubjectDTO> getSubjects(String name, UUID coursePublicId, Pageable pageable);

    SubjectDTO getSubjectByPublicId(UUID publicId);

    SubjectDTO createSubject(CreateSubjectRequest request);

    SubjectDTO updateSubject(UUID publicId, UpdateSubjectRequest request);

    void deleteSubject(UUID publicId);
}
