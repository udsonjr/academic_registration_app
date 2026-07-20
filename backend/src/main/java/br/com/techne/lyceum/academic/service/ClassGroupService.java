package br.com.techne.lyceum.academic.service;

import br.com.techne.lyceum.academic.dto.ClassGroupDTO;
import br.com.techne.lyceum.academic.dto.CreateClassGroupRequest;
import br.com.techne.lyceum.academic.dto.UpdateClassGroupRequest;
import java.util.List;
import java.util.UUID;

public interface ClassGroupService {

    List<ClassGroupDTO> getClassGroups();

    ClassGroupDTO getClassGroupByPublicId(UUID publicId);

    ClassGroupDTO createClassGroup(CreateClassGroupRequest request);

    ClassGroupDTO updateClassGroup(UUID publicId, UpdateClassGroupRequest request);

    void deleteClassGroup(UUID publicId);
}
