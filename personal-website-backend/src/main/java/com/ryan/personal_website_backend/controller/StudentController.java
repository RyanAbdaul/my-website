package com.ryan.personal_website_backend.controller;

import com.ryan.personal_website_backend.dto.CreateStudentDTO;
import com.ryan.personal_website_backend.dto.ResponseStudentDTO;
import com.ryan.personal_website_backend.dto.UpdatePasswordDTO;
import com.ryan.personal_website_backend.dto.UpdateStudentDTO;
import com.ryan.personal_website_backend.service.StudentService;
import jakarta.validation.Valid;
import lombok.AllArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@AllArgsConstructor
@RequestMapping("/students")
public class StudentController {
    private final StudentService studentService;
    @PostMapping
    public ResponseStudentDTO createNewStudent(@Valid @RequestBody CreateStudentDTO newStudent){
        return studentService.createStudent(newStudent);
    }
    @GetMapping
    public List<ResponseStudentDTO> getStudents(){
        return studentService.getStudents();
    }
    @GetMapping("/{id}")
    public ResponseStudentDTO getStudentById(@PathVariable UUID id){
        return studentService.getStudentById(id);
    }
    @PutMapping("/{id}")
    public ResponseStudentDTO updateStudent(@PathVariable UUID id, @Valid @RequestBody UpdateStudentDTO updateInput){
        return studentService.updateStudent(id, updateInput);
    }
    @DeleteMapping("/{id}")
    public void deleteStudent(@PathVariable UUID id){
        studentService.deleteStudentAccount(id);
    }
    @PostMapping("/{id}")
    public void updatePassword(@PathVariable  UUID id, @Valid @RequestBody UpdatePasswordDTO updatePassword){
        studentService.updatePassword(id, updatePassword);
    }
}
