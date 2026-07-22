package br.com.techne.lyceum.academic.service;

import br.com.techne.lyceum.academic.dto.ClassGroupDTO;
import br.com.techne.lyceum.academic.dto.CreateClassGroupRequest;
import br.com.techne.lyceum.academic.dto.PageResponse;
import br.com.techne.lyceum.academic.dto.UpdateClassGroupRequest;
import java.util.UUID;
import org.springframework.data.domain.Pageable;

public interface ClassGroupService {

    PageResponse<ClassGroupDTO> getClassGroups(
            String name,
            Boolean openForEnrollment,
            UUID subjectPublicId,
            UUID coursePublicId,
            Pageable pageable);

    ClassGroupDTO getClassGroupByPublicId(UUID publicId);

    ClassGroupDTO createClassGroup(CreateClassGroupRequest request);

    ClassGroupDTO updateClassGroup(UUID publicId, UpdateClassGroupRequest request);

    void deleteClassGroup(UUID publicId);
}
