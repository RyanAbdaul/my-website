package com.ryan.personal_website_backend.course;

import com.ryan.personal_website_backend.course.CreateEnrollmentDTO;
import com.ryan.personal_website_backend.course.ResponseEnrollmentDTO;
import com.ryan.personal_website_backend.course.Course;
import com.ryan.personal_website_backend.course.Enrollment;
import com.ryan.personal_website_backend.student.Student;
import com.ryan.personal_website_backend.common.CourseDoesNotExistException;
import com.ryan.personal_website_backend.common.CourseIsNotFreeException;
import com.ryan.personal_website_backend.common.EnrollmentAlreadyExistsException;
import com.ryan.personal_website_backend.common.EnrollmentDoesNotExistException;
import com.ryan.personal_website_backend.common.StudentDoesNotExistException;
import com.ryan.personal_website_backend.course.CourseRepo;
import com.ryan.personal_website_backend.course.EnrollmentRepo;
import com.ryan.personal_website_backend.student.StudentRepo;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

@Service
@AllArgsConstructor
public class EnrollmentService {

    private final EnrollmentRepo enrollmentRepo;
    private final StudentRepo studentRepo;
    private final CourseRepo courseRepo;

    public ResponseEnrollmentDTO createEnrollment(CreateEnrollmentDTO input) {
        Student student = studentRepo.findById(input.getStudentId())
                .orElseThrow(() -> new StudentDoesNotExistException("Student does not exist"));
        Course course = courseRepo.findById(input.getCourseId())
                .orElseThrow(() -> new CourseDoesNotExistException("Course does not exist"));
        return createEnrollment(student, course);
    }

    public ResponseEnrollmentDTO enrollCurrentUser(String email, UUID courseId) {
        Student student = studentRepo.findByUser_Email(email)
                .orElseThrow(() -> new StudentDoesNotExistException("Student does not exist"));
        Course course = courseRepo.findById(courseId)
                .orElseThrow(() -> new CourseDoesNotExistException("Course does not exist"));
        return createEnrollment(student, course);
    }

    private ResponseEnrollmentDTO createEnrollment(Student student, Course course) {
        boolean alreadyEnrolled = enrollmentRepo.findByStudent_Id(student.getId())
                .stream()
                .anyMatch(e -> e.getCourse().getId().equals(course.getId()));

        if (alreadyEnrolled) {
            throw new EnrollmentAlreadyExistsException("Student is already enrolled in this course");
        }

        if (course.getPrice().compareTo(BigDecimal.ZERO) != 0) {
            throw new CourseIsNotFreeException("Current course is not free");
        }

        Enrollment enrollment = new Enrollment();
        enrollment.setStudent(student);
        enrollment.setCourse(course);
        enrollment.setPaymentStatus("PENDING");

        Enrollment saved = enrollmentRepo.save(enrollment);

        return toDTO(saved);
    }

    public List<ResponseEnrollmentDTO> getEnrollmentsByStudentId(UUID studentId) {
        return enrollmentRepo.findByStudent_Id(studentId)
                .stream()
                .map(this::toDTO)
                .toList();
    }

    public List<ResponseEnrollmentDTO> getEnrollmentsByCourseId(UUID courseId) {
        return enrollmentRepo.findByCourse_Id(courseId)
                .stream()
                .map(this::toDTO)
                .toList();
    }

    public boolean isUserEnrolled(String email, UUID courseId) {
        return enrollmentRepo.existsByStudent_User_EmailAndCourse_Id(email, courseId);
    }

    public void deleteEnrollment(UUID id) {
        Enrollment enrollment = enrollmentRepo.findById(id)
                .orElseThrow(() -> new EnrollmentDoesNotExistException("Enrollment does not exist"));
        enrollmentRepo.delete(enrollment);
    }

    private ResponseEnrollmentDTO toDTO(Enrollment e) {
        return new ResponseEnrollmentDTO(
                e.getId(),
                e.getStudent().getId(),
                e.getStudent().getName(),
                e.getCourse().getId(),
                e.getCourse().getTitle(),
                e.getPaymentStatus(),
                e.getEnrolledAt()
        );
    }
}
