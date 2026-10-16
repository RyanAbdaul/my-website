package com.ryan.personal_website_backend.course;

import com.ryan.personal_website_backend.course.CourseProgressResponseDTO;
import com.ryan.personal_website_backend.course.CreateProgressDTO;
import com.ryan.personal_website_backend.course.ResponseProgressDTO;
import com.ryan.personal_website_backend.course.UpdateProgressDTO;
import com.ryan.personal_website_backend.course.Course;
import com.ryan.personal_website_backend.course.CourseProgress;
import com.ryan.personal_website_backend.course.Lesson;
import com.ryan.personal_website_backend.course.Progress;
import com.ryan.personal_website_backend.student.Student;
import com.ryan.personal_website_backend.common.CourseDoesNotExistException;
import com.ryan.personal_website_backend.common.LessonNotFoundException;
import com.ryan.personal_website_backend.common.ProgressAlreadyExistsException;
import com.ryan.personal_website_backend.common.ProgressNotFoundException;
import com.ryan.personal_website_backend.common.StudentDoesNotExistException;
import com.ryan.personal_website_backend.course.CourseProgressRepo;
import com.ryan.personal_website_backend.course.CourseRepo;
import com.ryan.personal_website_backend.course.LessonRepo;
import com.ryan.personal_website_backend.course.ProgressRepo;
import com.ryan.personal_website_backend.student.StudentRepo;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.sql.Timestamp;
import java.util.List;
import java.util.UUID;

@Service
@AllArgsConstructor
public class ProgressService {

    private final ProgressRepo progressRepo;
    private final StudentRepo studentRepo;
    private final LessonRepo lessonRepo;
    private final CourseProgressRepo courseProgressRepo;
    private final CourseRepo courseRepo;

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

    @Transactional(readOnly = true)
    public CourseProgressResponseDTO getCourseProgressForCurrentUser(String email, UUID courseId) {
        Student student = studentRepo.findByUser_Email(email)
                .orElseThrow(() -> new StudentDoesNotExistException("Student not found for current user"));

        List<UUID> completedLessonIds = progressRepo.findByStudentIdAndLesson_Course_Id(student.getId(), courseId)
                .stream()
                .filter(progress -> Boolean.TRUE.equals(progress.getIsComplete()))
                .map(progress -> progress.getLesson().getId())
                .toList();

        UUID lastLessonId = courseProgressRepo.findByStudentIdAndCourseId(student.getId(), courseId)
                .map(courseProgress -> courseProgress.getLastLesson() != null ? courseProgress.getLastLesson().getId() : null)
                .orElse(null);

        return CourseProgressResponseDTO.builder()
                .completedLessonIds(completedLessonIds)
                .lastLessonId(lastLessonId)
                .build();
    }

    @Transactional
    public ResponseProgressDTO setLessonCompletionForCurrentUser(String email, UUID lessonId, boolean isComplete) {
        Student student = studentRepo.findByUser_Email(email)
                .orElseThrow(() -> new StudentDoesNotExistException("Student not found for current user"));
        Lesson lesson = lessonRepo.findById(lessonId)
                .orElseThrow(() -> new LessonNotFoundException("Lesson with ID " + lessonId + " not found"));

        Progress progress = progressRepo.findByStudentIdAndLessonId(student.getId(), lessonId)
                .orElseGet(() -> {
                    Progress created = new Progress();
                    created.setStudent(student);
                    created.setLesson(lesson);
                    return created;
                });

        progress.setIsComplete(isComplete);
        Progress saved = progressRepo.save(progress);
        return mapToResponseDTO(saved);
    }

    @Transactional
    public CourseProgressResponseDTO setLastLessonForCurrentUser(String email, UUID courseId, UUID lessonId) {
        Student student = studentRepo.findByUser_Email(email)
                .orElseThrow(() -> new StudentDoesNotExistException("Student not found for current user"));
        Course course = courseRepo.findById(courseId)
                .orElseThrow(() -> new CourseDoesNotExistException("Course with ID " + courseId + " does not exist"));
        Lesson lesson = lessonRepo.findById(lessonId)
                .orElseThrow(() -> new LessonNotFoundException("Lesson with ID " + lessonId + " not found"));

        CourseProgress courseProgress = courseProgressRepo.findByStudentIdAndCourseId(student.getId(), courseId)
                .orElseGet(() -> {
                    CourseProgress created = new CourseProgress();
                    created.setStudent(student);
                    created.setCourse(course);
                    return created;
                });

        courseProgress.setLastLesson(lesson);
        courseProgress.setUpdatedAt(new Timestamp(System.currentTimeMillis()));
        courseProgressRepo.save(courseProgress);

        return getCourseProgressForCurrentUser(email, courseId);
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
