package com.ryan.personal_website_backend.course;

import com.ryan.personal_website_backend.course.CreateLessonDTO;
import com.ryan.personal_website_backend.course.ResponseLessonDTO;
import com.ryan.personal_website_backend.course.UpdateLessonDTO;
import com.ryan.personal_website_backend.course.LessonService;
import jakarta.validation.Valid;
import lombok.AllArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@AllArgsConstructor
@RequestMapping("/lessons")
public class LessonController {

    private final LessonService lessonService;

    @GetMapping("/course/{courseId}")
    public List<ResponseLessonDTO> getLessonsByCourseId(@PathVariable UUID courseId) {
        return lessonService.getLessonsByCourseId(courseId);
    }

    @GetMapping("/course/{courseId}/search")
    public List<ResponseLessonDTO> getLessonsByCourseIdAndTitle(
            @PathVariable UUID courseId,
            @RequestParam String title) {
        return lessonService.getLessonsByCourseIdAndTitle(courseId, title);
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public ResponseLessonDTO createLesson(@Valid @RequestBody CreateLessonDTO dto) {
        return lessonService.createLesson(dto);
    }

    @PutMapping("/{id}")
    public ResponseLessonDTO updateLesson(@PathVariable UUID id, @Valid @RequestBody UpdateLessonDTO dto) {
        return lessonService.updateLesson(id, dto);
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteLesson(@PathVariable UUID id) {
        lessonService.deleteLesson(id);
    }
}
