package com.ryan.personal_website_backend.controller;

import com.ryan.personal_website_backend.dto.CourseProgressResponseDTO;
import com.ryan.personal_website_backend.dto.CreateProgressDTO;
import com.ryan.personal_website_backend.dto.ResponseProgressDTO;
import com.ryan.personal_website_backend.dto.SetLastLessonDTO;
import com.ryan.personal_website_backend.dto.UpdateProgressDTO;
import com.ryan.personal_website_backend.service.ProgressService;
import jakarta.validation.Valid;
import lombok.AllArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@AllArgsConstructor
@RequestMapping("/progress")
public class ProgressController {

    private final ProgressService progressService;

    @GetMapping("/student/{studentId}")
    public List<ResponseProgressDTO> getProgressByStudentId(@PathVariable UUID studentId) {
        return progressService.getProgressByStudentId(studentId);
    }

    @GetMapping("/lesson/{lessonId}")
    public List<ResponseProgressDTO> getProgressByLessonId(@PathVariable UUID lessonId) {
        return progressService.getProgressByLessonId(lessonId);
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public ResponseProgressDTO createProgress(@Valid @RequestBody CreateProgressDTO dto) {
        return progressService.createProgress(dto);
    }

    @PutMapping("/{id}")
    public ResponseProgressDTO updateProgress(@PathVariable UUID id, @Valid @RequestBody UpdateProgressDTO dto) {
        return progressService.updateProgress(id, dto);
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteProgress(@PathVariable UUID id) {
        progressService.deleteProgress(id);
    }

    @GetMapping("/course/{courseId}/me")
    public CourseProgressResponseDTO getCourseProgressForCurrentUser(
            @PathVariable UUID courseId,
            Authentication authentication) {
        return progressService.getCourseProgressForCurrentUser(authentication.getName(), courseId);
    }

    @PutMapping("/lesson/{lessonId}/me")
    public ResponseProgressDTO setLessonCompletionForCurrentUser(
            @PathVariable UUID lessonId,
            @Valid @RequestBody UpdateProgressDTO dto,
            Authentication authentication) {
        return progressService.setLessonCompletionForCurrentUser(authentication.getName(), lessonId, dto.getIsComplete());
    }

    @PutMapping("/course/{courseId}/last-lesson")
    public CourseProgressResponseDTO setLastLessonForCurrentUser(
            @PathVariable UUID courseId,
            @Valid @RequestBody SetLastLessonDTO dto,
            Authentication authentication) {
        return progressService.setLastLessonForCurrentUser(authentication.getName(), courseId, dto.getLessonId());
    }
}
