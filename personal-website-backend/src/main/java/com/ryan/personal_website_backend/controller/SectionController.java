package com.ryan.personal_website_backend.controller;

import com.ryan.personal_website_backend.dto.SectionRequestDTO;
import com.ryan.personal_website_backend.dto.SectionResponseDTO;
import com.ryan.personal_website_backend.service.SectionService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/sections")
@RequiredArgsConstructor
public class SectionController {

    private final SectionService sectionService;

    @PostMapping("/course/{courseId}")
    @ResponseStatus(HttpStatus.CREATED)
    public SectionResponseDTO createSection(
            @PathVariable UUID courseId,
            @RequestBody SectionRequestDTO sectionRequest) {
        return sectionService.createSection(courseId, sectionRequest);
    }

    @GetMapping("/course/{courseId}")
    @ResponseStatus(HttpStatus.OK)
    public List<SectionResponseDTO> getAllSectionsByCourse(@PathVariable UUID courseId) {
        return sectionService.getAllSectionsByCourse(courseId);
    }

    @GetMapping("/{id}")
    @ResponseStatus(HttpStatus.OK)
    public SectionResponseDTO getSectionById(@PathVariable UUID id) {
        return sectionService.getSectionById(id);
    }

    @GetMapping("/{id}/course/{courseId}")
    @ResponseStatus(HttpStatus.OK)
    public SectionResponseDTO getSectionByIdAndCourseId(
            @PathVariable UUID id,
            @PathVariable UUID courseId) {
        return sectionService.getSectionByIdAndCourseId(id, courseId);
    }

    @PutMapping("/{id}")
    @ResponseStatus(HttpStatus.OK)
    public SectionResponseDTO updateSection(
            @PathVariable UUID id,
            @RequestBody SectionRequestDTO sectionRequest) {
        return sectionService.updateSection(id, sectionRequest);
    }

    @PatchMapping("/{id}/order")
    @ResponseStatus(HttpStatus.OK)
    public SectionResponseDTO updateSectionOrder(
            @PathVariable UUID id,
            @RequestParam Integer orderIndex) {
        return sectionService.updateSectionOrder(id, orderIndex);
    }

    @PutMapping("/course/{courseId}/reorder")
    @ResponseStatus(HttpStatus.OK)
    public List<SectionResponseDTO> reorderSections(
            @PathVariable UUID courseId,
            @RequestBody List<UUID> sectionIdsInOrder) {
        return sectionService.reorderSections(courseId, sectionIdsInOrder);
    }

    @PatchMapping("/{id}/course/{courseId}/move-up")
    @ResponseStatus(HttpStatus.OK)
    public SectionResponseDTO moveSectionUp(
            @PathVariable UUID id,
            @PathVariable UUID courseId) {
        return sectionService.moveSectionUp(id, courseId);
    }

    @PatchMapping("/{id}/course/{courseId}/move-down")
    @ResponseStatus(HttpStatus.OK)
    public SectionResponseDTO moveSectionDown(
            @PathVariable UUID id,
            @PathVariable UUID courseId) {
        return sectionService.moveSectionDown(id, courseId);
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteSection(@PathVariable UUID id) {
        sectionService.deleteSection(id);
    }

    @DeleteMapping("/{id}/course/{courseId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteSectionByIdAndCourseId(
            @PathVariable UUID id,
            @PathVariable UUID courseId) {
        sectionService.deleteSectionByIdAndCourseId(id, courseId);
    }

    @DeleteMapping("/course/{courseId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteAllSectionsByCourse(@PathVariable UUID courseId) {
        sectionService.deleteAllSectionsByCourse(courseId);
    }

    @GetMapping("/{id}/exists")
    @ResponseStatus(HttpStatus.OK)
    public boolean existsById(@PathVariable UUID id) {
        return sectionService.existsById(id);
    }

    @GetMapping("/{id}/course/{courseId}/exists")
    @ResponseStatus(HttpStatus.OK)
    public boolean existsByIdAndCourseId(
            @PathVariable UUID id,
            @PathVariable UUID courseId) {
        return sectionService.existsByIdAndCourseId(id, courseId);
    }

    @GetMapping("/course/{courseId}/count")
    @ResponseStatus(HttpStatus.OK)
    public long getSectionCountByCourse(@PathVariable UUID courseId) {
        return sectionService.getSectionCountByCourse(courseId);
    }
}
