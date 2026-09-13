package com.ryan.personal_website_backend.service;

import com.ryan.personal_website_backend.dto.CreateProgressDTO;
import com.ryan.personal_website_backend.dto.ResponseProgressDTO;
import com.ryan.personal_website_backend.dto.UpdateProgressDTO;
import com.ryan.personal_website_backend.entity.Lesson;
import com.ryan.personal_website_backend.entity.Progress;
import com.ryan.personal_website_backend.entity.Student;
import com.ryan.personal_website_backend.exception.LessonNotFoundException;
import com.ryan.personal_website_backend.exception.ProgressAlreadyExistsException;
import com.ryan.personal_website_backend.exception.ProgressNotFoundException;
import com.ryan.personal_website_backend.exception.StudentDoesNotExistException;
import com.ryan.personal_website_backend.repository.LessonRepo;
import com.ryan.personal_website_backend.repository.ProgressRepo;
import com.ryan.personal_website_backend.repository.StudentRepo;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;

@Service
@AllArgsConstructor
public class ProgressService {

    private final ProgressRepo progressRepo;
    private final StudentRepo studentRepo;
    private final LessonRepo lessonRepo;

    public List<ResponseProgressDTO> getProgressByStudentId(UUID studentId) {
        if (!studentRepo.existsById(studentId)) {
            throw new StudentDoesNotExistException("Student with ID " + studentId + " does not exist");
        }
        return progressRepo.findByStudentId(studentId)
                .stream()
                .map(this::mapToResponseDTO)
                .toList();
    }

    public List<ResponseProgressDTO> getProgressByLessonId(UUID lessonId) {
        if (!lessonRepo.existsById(lessonId)) {
            throw new LessonNotFoundException("Lesson with ID " + lessonId + " not found");
        }
        return progressRepo.findByLessonId(lessonId)
                .stream()
                .map(this::mapToResponseDTO)
                .toList();
    }

    public ResponseProgressDTO createProgress(CreateProgressDTO dto) {
        Student student = studentRepo.findById(dto.getStudentId())
                .orElseThrow(() -> new StudentDoesNotExistException("Student with ID " + dto.getStudentId() + " does not exist"));

        Lesson lesson = lessonRepo.findById(dto.getLessonId())
                .orElseThrow(() -> new LessonNotFoundException("Lesson with ID " + dto.getLessonId() + " not found"));

        if (progressRepo.existsByStudentIdAndLessonId(dto.getStudentId(), dto.getLessonId())) {
            throw new ProgressAlreadyExistsException("Progress record already exists for this student and lesson");
        }

        Progress progress = new Progress();
        progress.setStudent(student);
        progress.setLesson(lesson);
        progress.setIsComplete(false);

        Progress saved = progressRepo.save(progress);
        return mapToResponseDTO(saved);
    }

    public ResponseProgressDTO updateProgress(UUID id, UpdateProgressDTO dto) {
        Progress progress = progressRepo.findById(id)
                .orElseThrow(() -> new ProgressNotFoundException("Progress record with ID " + id + " not found"));

        progress.setIsComplete(dto.getIsComplete());

        Progress updated = progressRepo.save(progress);
        return mapToResponseDTO(updated);
    }

    public void deleteProgress(UUID id) {
        if (!progressRepo.existsById(id)) {
            throw new ProgressNotFoundException("Progress record with ID " + id + " not found");
        }
        progressRepo.deleteById(id);
    }

    private ResponseProgressDTO mapToResponseDTO(Progress progress) {
        return ResponseProgressDTO.builder()
                .id(progress.getId())
                .studentId(progress.getStudent().getId())
                .studentName(progress.getStudent().getName())
                .lessonId(progress.getLesson().getId())
                .lessonTitle(progress.getLesson().getTitle())
                .isComplete(progress.getIsComplete())
                .build();
    }
}
