package com.ryan.personal_website_backend.course;
import com.ryan.personal_website_backend.student.Student;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.sql.Timestamp;
import java.util.UUID;

@Entity
@Table(name = "course_progress", uniqueConstraints = @UniqueConstraint(columnNames = { "student_id", "course_id" }))
@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
public class CourseProgress {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @ManyToOne
    @JoinColumn(name = "student_id", nullable = false)
    private Student student;

    @ManyToOne
    @JoinColumn(name = "course_id", nullable = false)
    private Course course;

    @ManyToOne
    @JoinColumn(name = "last_lesson_id")
    private Lesson lastLesson;

    @Column(name = "updated_at", nullable = false)
    private Timestamp updatedAt;
}
