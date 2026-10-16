package com.ryan.personal_website_backend.service;

import com.ryan.personal_website_backend.student.StudentService;

import com.ryan.personal_website_backend.student.CreateStudentDTO;
import com.ryan.personal_website_backend.student.ResponseStudentDTO;
import com.ryan.personal_website_backend.student.Student;
import com.ryan.personal_website_backend.auth.User;
import com.ryan.personal_website_backend.common.StudentAlreadyExistsException;
import com.ryan.personal_website_backend.student.StudentRepo;
import com.ryan.personal_website_backend.auth.UserRepo;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class StudentServiceTest {

    @Mock
    private StudentRepo studentRepo;

    @Mock
    private UserRepo userRepo;

    @Mock
    private PasswordEncoder passwordEncoder;

    @InjectMocks
    private StudentService studentService;

    @Test
    void createStudent_success() {
        CreateStudentDTO newStudent = new CreateStudentDTO();
        newStudent.setName("Test");
        newStudent.setEmail("test@test.com");
        newStudent.setPassword("123456789");

        when(userRepo.existsByEmail("test@test.com")).thenReturn(false);

        when(passwordEncoder.encode("123456789")).thenReturn("hashedPassword123");

        Student savedStudent = new Student();
        savedStudent.setId(UUID.randomUUID());
        savedStudent.setName("Test");
        savedStudent.setUser(new User(null, "test@test.com", "hashedPassword123", null, null, null));

        when(userRepo.save(any(User.class))).thenAnswer(invocation -> invocation.getArgument(0));
        when(studentRepo.save(any(Student.class))).thenReturn(savedStudent);

        ResponseStudentDTO result = studentService.createStudent(newStudent);

        assertEquals("Test", result.getName());
        assertEquals("test@test.com", result.getEmail());
    }
    @Test
    void createStudent_fail() {
        CreateStudentDTO input = new CreateStudentDTO();
        input.setName("Test");
        input.setEmail("test@test.com");
        input.setPassword("123456789");

        when(userRepo.existsByEmail("test@test.com")).thenReturn(true);

        assertThrows(StudentAlreadyExistsException.class, () -> {
            studentService.createStudent(input);
        });
    }
}
