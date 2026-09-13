package com.ryan.personal_website_backend.service;

import com.ryan.personal_website_backend.dto.CreateLessonDTO;
import com.ryan.personal_website_backend.dto.ResponseLessonDTO;
import com.ryan.personal_website_backend.dto.UpdateLessonDTO;
import com.ryan.personal_website_backend.entity.Course;
import com.ryan.personal_website_backend.entity.Lesson;
import com.ryan.personal_website_backend.exception.CourseDoesNotExistException;
import com.ryan.personal_website_backend.exception.LessonNotFoundException;
import com.ryan.personal_website_backend.repository.CourseRepo;
import com.ryan.personal_website_backend.repository.LessonRepo;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;

@Service
@AllArgsConstructor
public class LessonService {

    private final LessonRepo lessonRepo;
    private final CourseRepo courseRepo;

    public List<ResponseLessonDTO> getLessonsByCourseId(UUID courseId) {
        if (!courseRepo.existsById(courseId)) {
            throw new CourseDoesNotExistException("Course with ID " + courseId + " does not exist");
        }
        return lessonRepo.findByCourseId(courseId)
                .stream()
                .map(this::mapToResponseDTO)
                .toList();
    }

    public List<ResponseLessonDTO> getLessonsByCourseIdAndTitle(UUID courseId, String title) {
        if (!courseRepo.existsById(courseId)) {
            throw new CourseDoesNotExistException("Course with ID " + courseId + " does not exist");
        }
        return lessonRepo.findByCourseIdAndTitleContainingIgnoreCase(courseId, title)
                .stream()
                .map(this::mapToResponseDTO)
                .toList();
    }

    public ResponseLessonDTO createLesson(CreateLessonDTO dto) {
        Course course = courseRepo.findById(dto.getCourseId())
                .orElseThrow(() -> new CourseDoesNotExistException("Course with ID " + dto.getCourseId() + " does not exist"));

        Lesson lesson = new Lesson();
        lesson.setCourse(course);
        lesson.setTitle(dto.getTitle());
        lesson.setVideoLink(dto.getVideoLink());

        Lesson saved = lessonRepo.save(lesson);
        return mapToResponseDTO(saved);
    }

    public ResponseLessonDTO updateLesson(UUID id, UpdateLessonDTO dto) {
        Lesson lesson = lessonRepo.findById(id)
                .orElseThrow(() -> new LessonNotFoundException("Lesson with ID " + id + " not found"));

        lesson.setTitle(dto.getTitle());
        lesson.setVideoLink(dto.getVideoLink());

        Lesson updated = lessonRepo.save(lesson);
        return mapToResponseDTO(updated);
    }

    public void deleteLesson(UUID id) {
        if (!lessonRepo.existsById(id)) {
            throw new LessonNotFoundException("Lesson with ID " + id + " not found");
        }
        lessonRepo.deleteById(id);
    }

    private ResponseLessonDTO mapToResponseDTO(Lesson lesson) {
        return ResponseLessonDTO.builder()
                .id(lesson.getId())
                .courseId(lesson.getCourse().getId())
                .title(lesson.getTitle())
                .videoLink(lesson.getVideoLink())
                .createTime(lesson.getCreateTime())
                .build();
    }
}
