package com.ryan.personal_website_backend.controller;

import com.ryan.personal_website_backend.dto.CreateProjectDTO;
import com.ryan.personal_website_backend.dto.ResponseProjectDTO;
import com.ryan.personal_website_backend.service.ProjectService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/projects")
@RequiredArgsConstructor
public class ProjectController {
    private final ProjectService projectService;
    @GetMapping public List<ResponseProjectDTO> getProjects() { return projectService.getProjects(); }
    @PostMapping @ResponseStatus(HttpStatus.CREATED) public ResponseProjectDTO create(@Valid @RequestBody CreateProjectDTO dto) { return projectService.create(dto); }
    @PutMapping("/{id}") public ResponseProjectDTO update(@PathVariable UUID id, @Valid @RequestBody CreateProjectDTO dto) { return projectService.update(id, dto); }
    @DeleteMapping("/{id}") @ResponseStatus(HttpStatus.NO_CONTENT) public void delete(@PathVariable UUID id) { projectService.delete(id); }
}
