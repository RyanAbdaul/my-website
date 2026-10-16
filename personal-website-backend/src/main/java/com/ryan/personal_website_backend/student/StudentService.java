package com.ryan.personal_website_backend.student;

import com.ryan.personal_website_backend.student.CreateStudentDTO;
import com.ryan.personal_website_backend.student.ResponseStudentDTO;
import com.ryan.personal_website_backend.student.UpdatePasswordDTO;
import com.ryan.personal_website_backend.student.UpdateStudentDTO;
import com.ryan.personal_website_backend.student.Student;
import com.ryan.personal_website_backend.auth.Authority;
import com.ryan.personal_website_backend.auth.User;
import com.ryan.personal_website_backend.common.InvalidPasswordException;
import com.ryan.personal_website_backend.common.StudentAlreadyExistsException;
import com.ryan.personal_website_backend.common.StudentDoesNotExistException;
import com.ryan.personal_website_backend.student.StudentRepo;
import com.ryan.personal_website_backend.auth.UserRepo;
import lombok.AllArgsConstructor;

import org.jspecify.annotations.NonNull;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;
import java.util.List;

@Service
@AllArgsConstructor
public class StudentService {

    private final StudentRepo studentRepo;
    private final UserRepo userRepo;
    private final PasswordEncoder passwordEncoder;

    @Transactional
    public ResponseStudentDTO createStudent(@NonNull CreateStudentDTO studentInput) {
        if (userRepo.existsByEmail(studentInput.getEmail())) {
            throw new StudentAlreadyExistsException("A student with this email already exists");
        }
        User user = new User(
                null,
                studentInput.getEmail(),
                passwordEncoder.encode(studentInput.getPassword()),
                null,
                null,
                List.of(new Authority("ROLE_STUDENT"))
        );
        User savedUser = userRepo.save(user);

        Student newStudent = new Student();
        newStudent.setName(studentInput.getName());
        newStudent.setUser(savedUser);
        Student savedStudent = studentRepo.save(newStudent);
        savedUser.setStudent(savedStudent);

        return new ResponseStudentDTO(savedStudent.getId(), savedStudent.getName(), savedUser.getEmail());
    }

    @Transactional(readOnly = true)
    public List<ResponseStudentDTO> getStudents() {
        List<Student> students = studentRepo.findAll();

        return students.stream()
                .map(s -> new ResponseStudentDTO(s.getId(), s.getName(), s.getUser() != null ? s.getUser().getEmail() : ""))
                .toList();
    }

    @Transactional(readOnly = true)
    public ResponseStudentDTO getStudentById(UUID id) {
        Student student = studentRepo.findById(id)
                .orElseThrow(() -> new StudentDoesNotExistException("Student does not exist"));
        return new ResponseStudentDTO(student.getId(), student.getName(), student.getUser() != null ? student.getUser().getEmail() : "");
    }

    @Transactional
    public ResponseStudentDTO updateStudent(UUID id, UpdateStudentDTO updateInput) {
        Student student = studentRepo.findById(id)
                .orElseThrow(() -> new StudentDoesNotExistException("Student does not exist"));
        if (student.getUser() != null) {
            student.getUser().setEmail(updateInput.getEmail());
            userRepo.save(student.getUser());
        }
        student.setName(updateInput.getName());
        Student updated = studentRepo.save(student);
        return new ResponseStudentDTO(updated.getId(), updated.getName(), updated.getUser() != null ? updated.getUser().getEmail() : "");
    }

    @Transactional
    public void deleteStudentAccount(UUID id) {
        Student student = studentRepo.findById(id)
                .orElseThrow(() -> new StudentDoesNotExistException("This student does not exist"));
        User user = student.getUser();
        if (user != null) {
            userRepo.delete(user);
        } else {
            studentRepo.delete(student);
        }
    }

    @Transactional
    public void updatePassword(UUID id, UpdatePasswordDTO updatePassword) {
        Student student = studentRepo.findById(id)
                .orElseThrow(() -> new StudentDoesNotExistException("This student does not exist"));

        User user = student.getUser();
        if (user == null) {
            throw new StudentDoesNotExistException("User account does not exist for this student");
        }
        if (!passwordEncoder.matches(updatePassword.getCurrentPassword(), user.getPassword())) {
            throw new InvalidPasswordException("Current password does not match");
        }
        user.setPassword(passwordEncoder.encode(updatePassword.getNewPassword()));
        userRepo.save(user);
    }
}
