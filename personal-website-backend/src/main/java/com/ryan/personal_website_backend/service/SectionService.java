package com.ryan.personal_website_backend.service;

import com.ryan.personal_website_backend.dto.SectionRequestDTO;
import com.ryan.personal_website_backend.dto.SectionResponseDTO;
import com.ryan.personal_website_backend.entity.Course;
import com.ryan.personal_website_backend.entity.Section;
import com.ryan.personal_website_backend.exception.CourseDoesNotExistException;
import com.ryan.personal_website_backend.repository.CourseRepo;
import com.ryan.personal_website_backend.repository.SectionRepo;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class SectionService {

    private final SectionRepo sectionRepo;
    private final CourseRepo courseRepo;

    // Create a new section
    @Transactional
    public SectionResponseDTO createSection(UUID courseId, SectionRequestDTO sectionRequest) {
        Course course = courseRepo.findById(courseId)
                .orElseThrow(() -> new CourseDoesNotExistException("Course not found with id: " + courseId));

        // Convert DTO to Entity
        Section section = mapToEntity(sectionRequest);
        section.setCourse(course);

        // Set order index if not provided (append to end)
        if (section.getOrderIndex() == null) {
            sectionRepo.findFirstByCourseIdOrderByOrderIndexDesc(courseId)
                    .ifPresentOrElse(
                            lastSection -> section.setOrderIndex(lastSection.getOrderIndex() + 1),
                            () -> section.setOrderIndex(1)
                    );
        }

        Section savedSection = sectionRepo.save(section);
        return mapToResponseDTO(savedSection);
    }

    // Get all sections for a course
    public List<SectionResponseDTO> getAllSectionsByCourse(UUID courseId) {
        return sectionRepo.findByCourseIdOrderByOrderIndexAsc(courseId)
                .stream()
                .map(this::mapToResponseDTO)
                .collect(Collectors.toList());
    }

    // Get a specific section
    public SectionResponseDTO getSectionById(UUID id) {
        Section section = sectionRepo.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Section not found with id: " + id));
        return mapToResponseDTO(section);
    }

    // Get a specific section ensuring it belongs to a course
    public SectionResponseDTO getSectionByIdAndCourseId(UUID id, UUID courseId) {
        Section section = sectionRepo.findByIdAndCourseId(id, courseId)
                .orElseThrow(() -> new EntityNotFoundException(
                        "Section not found with id: " + id + " and courseId: " + courseId));
        return mapToResponseDTO(section);
    }

    // Update a section
    @Transactional
    public SectionResponseDTO updateSection(UUID id, SectionRequestDTO sectionRequest) {
        Section section = sectionRepo.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Section not found with id: " + id));

        // Update fields from DTO
        if (sectionRequest.getTitle() != null) {
            section.setTitle(sectionRequest.getTitle());
        }
        if (sectionRequest.getDescription() != null) {
            section.setDescription(sectionRequest.getDescription());
        }
        if (sectionRequest.getOrderIndex() != null) {
            section.setOrderIndex(sectionRequest.getOrderIndex());
        }

        Section updatedSection = sectionRepo.save(section);
        return mapToResponseDTO(updatedSection);
    }

    // Update section order
    @Transactional
    public SectionResponseDTO updateSectionOrder(UUID id, Integer newOrderIndex) {
        Section section = sectionRepo.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Section not found with id: " + id));
        section.setOrderIndex(newOrderIndex);
        Section updatedSection = sectionRepo.save(section);
        return mapToResponseDTO(updatedSection);
    }

    // Reorder sections within a course
    @Transactional
    public List<SectionResponseDTO> reorderSections(UUID courseId, List<UUID> sectionIdsInOrder) {
        List<Section> sections = sectionRepo.findByCourseId(courseId);

        for (int i = 0; i < sectionIdsInOrder.size(); i++) {
            UUID sectionId = sectionIdsInOrder.get(i);
            Section section = sections.stream()
                    .filter(s -> s.getId().equals(sectionId))
                    .findFirst()
                    .orElseThrow(() -> new EntityNotFoundException(
                            "Section not found with id: " + sectionId + " in course: " + courseId));

            section.setOrderIndex(i + 1);
            sectionRepo.save(section);
        }

        return sectionRepo.findByCourseIdOrderByOrderIndexAsc(courseId)
                .stream()
                .map(this::mapToResponseDTO)
                .collect(Collectors.toList());
    }

    // Move section up (decrease order index)
    @Transactional
    public SectionResponseDTO moveSectionUp(UUID id, UUID courseId) {
        Section section = sectionRepo.findByIdAndCourseId(id, courseId)
                .orElseThrow(() -> new EntityNotFoundException(
                        "Section not found with id: " + id + " and courseId: " + courseId));

        if (section.getOrderIndex() != null && section.getOrderIndex() > 1) {
            List<Section> sections = sectionRepo.findByCourseIdOrderByOrderIndexAsc(courseId);
            int currentIndex = sections.indexOf(section);

            if (currentIndex > 0) {
                Section previousSection = sections.get(currentIndex - 1);

                // Swap order indexes
                Integer tempOrder = section.getOrderIndex();
                section.setOrderIndex(previousSection.getOrderIndex());
                previousSection.setOrderIndex(tempOrder);

                sectionRepo.save(section);
                sectionRepo.save(previousSection);
            }
        }

        return mapToResponseDTO(section);
    }

    // Move section down (increase order index)
    @Transactional
    public SectionResponseDTO moveSectionDown(UUID id, UUID courseId) {
        Section section = sectionRepo.findByIdAndCourseId(id, courseId)
                .orElseThrow(() -> new EntityNotFoundException(
                        "Section not found with id: " + id + " and courseId: " + courseId));

        List<Section> sections = sectionRepo.findByCourseIdOrderByOrderIndexAsc(courseId);
        int currentIndex = sections.indexOf(section);

        if (currentIndex >= 0 && currentIndex < sections.size() - 1) {
            Section nextSection = sections.get(currentIndex + 1);

            // Swap order indexes
            Integer tempOrder = section.getOrderIndex();
            section.setOrderIndex(nextSection.getOrderIndex());
            nextSection.setOrderIndex(tempOrder);

            sectionRepo.save(section);
            sectionRepo.save(nextSection);
        }

        return mapToResponseDTO(section);
    }

    // Delete a section
    @Transactional
    public void deleteSection(UUID id) {
        Section section = sectionRepo.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Section not found with id: " + id));
        sectionRepo.delete(section);
    }

    // Delete a section ensuring it belongs to a course
    @Transactional
    public void deleteSectionByIdAndCourseId(UUID id, UUID courseId) {
        Section section = sectionRepo.findByIdAndCourseId(id, courseId)
                .orElseThrow(() -> new EntityNotFoundException(
                        "Section not found with id: " + id + " and courseId: " + courseId));
        sectionRepo.delete(section);
    }

    // Delete all sections for a course
    @Transactional
    public void deleteAllSectionsByCourse(UUID courseId) {
        sectionRepo.deleteByCourseId(courseId);
    }

    // Check if section exists
    public boolean existsById(UUID id) {
        return sectionRepo.existsById(id);
    }

    // Check if section exists for a course
    public boolean existsByIdAndCourseId(UUID id, UUID courseId) {
        return sectionRepo.existsByIdAndCourseId(id, courseId);
    }

    // Get section count for a course
    public long getSectionCountByCourse(UUID courseId) {
        return sectionRepo.countByCourseId(courseId);
    }

    // Helper method to map SectionRequestDTO to Section entity
    private Section mapToEntity(SectionRequestDTO dto) {
        Section section = new Section();
        section.setTitle(dto.getTitle());
        section.setDescription(dto.getDescription());
        section.setOrderIndex(dto.getOrderIndex());
        return section;
    }

    // Helper method to map Section entity to SectionResponseDTO
    private SectionResponseDTO mapToResponseDTO(Section section) {
        return SectionResponseDTO.builder()
                .id(section.getId())
                .title(section.getTitle())
                .description(section.getDescription())
                .orderIndex(section.getOrderIndex())
                .createTime(section.getCreateTime())
                .courseId(section.getCourse() != null ? section.getCourse().getId() : null)
                .build();
    }
}