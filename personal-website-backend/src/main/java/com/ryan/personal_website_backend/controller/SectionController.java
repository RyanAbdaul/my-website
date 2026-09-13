package com.ryan.personal_website_backend.controller;

import com.ryan.personal_website_backend.dto.SectionRequestDTO;
import com.ryan.personal_website_backend.dto.SectionResponseDTO;
import com.ryan.personal_website_backend.service.SectionService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/sections")
@RequiredArgsConstructor
@Tag(name = "Section Management", description = "APIs for managing course sections")
public class SectionController {

    private final SectionService sectionService;

    // Create a new section for a course
    @PostMapping("/course/{courseId}")
    @ResponseStatus(HttpStatus.CREATED)
    @Operation(summary = "Create a new section", description = "Creates a new section for a specific course")
    public SectionResponseDTO createSection(
            @PathVariable UUID courseId,
            @RequestBody SectionRequestDTO sectionRequest) {
        return sectionService.createSection(courseId, sectionRequest);
    }

    // Get all sections for a course
    @GetMapping("/course/{courseId}")
    @ResponseStatus(HttpStatus.OK)
    @Operation(summary = "Get all sections by course", description = "Retrieves all sections for a specific course")
    public List<SectionResponseDTO> getAllSectionsByCourse(@PathVariable UUID courseId) {
        return sectionService.getAllSectionsByCourse(courseId);
    }

    // Get a specific section by ID
    @GetMapping("/{id}")
    @ResponseStatus(HttpStatus.OK)
    @Operation(summary = "Get section by ID", description = "Retrieves a specific section by its ID")
    public SectionResponseDTO getSectionById(@PathVariable UUID id) {
        return sectionService.getSectionById(id);
    }

    // Get a specific section ensuring it belongs to a course
    @GetMapping("/{id}/course/{courseId}")
    @ResponseStatus(HttpStatus.OK)
    @Operation(summary = "Get section by ID and course", description = "Retrieves a section ensuring it belongs to a specific course")
    public SectionResponseDTO getSectionByIdAndCourseId(
            @PathVariable UUID id,
            @PathVariable UUID courseId) {
        return sectionService.getSectionByIdAndCourseId(id, courseId);
    }

    // Update a section
    @PutMapping("/{id}")
    @ResponseStatus(HttpStatus.OK)
    @Operation(summary = "Update section", description = "Updates an existing section")
    public SectionResponseDTO updateSection(
            @PathVariable UUID id,
            @RequestBody SectionRequestDTO sectionRequest) {
        return sectionService.updateSection(id, sectionRequest);
    }

    // Update section order
    @PatchMapping("/{id}/order")
    @ResponseStatus(HttpStatus.OK)
    @Operation(summary = "Update section order", description = "Updates the order index of a section")
    public SectionResponseDTO updateSectionOrder(
            @PathVariable UUID id,
            @RequestParam Integer orderIndex) {
        return sectionService.updateSectionOrder(id, orderIndex);
    }

    // Reorder sections within a course
    @PutMapping("/course/{courseId}/reorder")
    @ResponseStatus(HttpStatus.OK)
    @Operation(summary = "Reorder sections", description = "Reorders sections within a course")
    public List<SectionResponseDTO> reorderSections(
            @PathVariable UUID courseId,
            @RequestBody List<UUID> sectionIdsInOrder) {
        return sectionService.reorderSections(courseId, sectionIdsInOrder);
    }

    // Move section up
    @PatchMapping("/{id}/course/{courseId}/move-up")
    @ResponseStatus(HttpStatus.OK)
    @Operation(summary = "Move section up", description = "Moves a section up in the course order")
    public SectionResponseDTO moveSectionUp(
            @PathVariable UUID id,
            @PathVariable UUID courseId) {
        return sectionService.moveSectionUp(id, courseId);
    }

    // Move section down
    @PatchMapping("/{id}/course/{courseId}/move-down")
    @ResponseStatus(HttpStatus.OK)
    @Operation(summary = "Move section down", description = "Moves a section down in the course order")
    public SectionResponseDTO moveSectionDown(
            @PathVariable UUID id,
            @PathVariable UUID courseId) {
        return sectionService.moveSectionDown(id, courseId);
    }

    // Delete a section
    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @Operation(summary = "Delete section", description = "Deletes a section by its ID")
    public void deleteSection(@PathVariable UUID id) {
        sectionService.deleteSection(id);
    }

    // Delete a section ensuring it belongs to a course
    @DeleteMapping("/{id}/course/{courseId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @Operation(summary = "Delete section by ID and course", description = "Deletes a section ensuring it belongs to a specific course")
    public void deleteSectionByIdAndCourseId(
            @PathVariable UUID id,
            @PathVariable UUID courseId) {
        sectionService.deleteSectionByIdAndCourseId(id, courseId);
    }

    // Delete all sections for a course
    @DeleteMapping("/course/{courseId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @Operation(summary = "Delete all sections by course", description = "Deletes all sections for a specific course")
    public void deleteAllSectionsByCourse(@PathVariable UUID courseId) {
        sectionService.deleteAllSectionsByCourse(courseId);
    }

    // Check if section exists
    @GetMapping("/{id}/exists")
    @ResponseStatus(HttpStatus.OK)
    @Operation(summary = "Check section exists", description = "Checks if a section exists by its ID")
    public boolean existsById(@PathVariable UUID id) {
        return sectionService.existsById(id);
    }

    // Check if section exists for a course
    @GetMapping("/{id}/course/{courseId}/exists")
    @ResponseStatus(HttpStatus.OK)
    @Operation(summary = "Check section exists in course", description = "Checks if a section exists for a specific course")
    public boolean existsByIdAndCourseId(
            @PathVariable UUID id,
            @PathVariable UUID courseId) {
        return sectionService.existsByIdAndCourseId(id, courseId);
    }

    // Get section count for a course
    @GetMapping("/course/{courseId}/count")
    @ResponseStatus(HttpStatus.OK)
    @Operation(summary = "Get section count", description = "Gets the total number of sections for a course")
    public long getSectionCountByCourse(@PathVariable UUID courseId) {
        return sectionService.getSectionCountByCourse(courseId);
    }
}