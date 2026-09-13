package com.ryan.personal_website_backend.service;

import com.ryan.personal_website_backend.dto.CreateProjectDTO;
import com.ryan.personal_website_backend.dto.ResponseProjectDTO;
import com.ryan.personal_website_backend.entity.Project;
import com.ryan.personal_website_backend.repository.ProjectRepo;
import com.ryan.personal_website_backend.repository.MemberRepo;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class ProjectService {
    private final ProjectRepo projectRepo;
    private final MemberRepo memberRepo;
    public List<ResponseProjectDTO> getProjects() { return projectRepo.findAllByOrderByDisplayOrderAsc().stream().map(this::response).toList(); }
    public ResponseProjectDTO create(CreateProjectDTO dto) { Project project = new Project(); apply(project, dto); return response(projectRepo.save(project)); }
    public ResponseProjectDTO update(UUID id, CreateProjectDTO dto) { Project project = projectRepo.findById(id).orElseThrow(() -> new IllegalArgumentException("Project not found")); apply(project, dto); return response(projectRepo.save(project)); }
    public void delete(UUID id) { projectRepo.deleteById(id); }
    private void apply(Project project, CreateProjectDTO dto) { project.setTitle(dto.title()); project.setDescription(dto.description()); project.setImagePath(dto.imagePath()); project.setProjectUrl(dto.projectUrl()); project.setDisplayOrder(dto.displayOrder() == null ? 999 : dto.displayOrder()); if (dto.memberId() != null) project.setMember(memberRepo.findById(dto.memberId()).orElseThrow(() -> new IllegalArgumentException("Member not found"))); }
    private ResponseProjectDTO response(Project project) { return new ResponseProjectDTO(project.getId(), project.getTitle(), project.getDescription(), project.getImagePath(), project.getProjectUrl(), project.getDisplayOrder()); }
}
