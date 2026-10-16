package com.ryan.personal_website_backend.dto;

import com.ryan.personal_website_backend.student.CreateStudentDTO;

import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import jakarta.validation.ValidatorFactory;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;

class CreateStudentDTOTest {

    private Validator validator;

    @BeforeEach
    void setUp() {
        ValidatorFactory factory = Validation.buildDefaultValidatorFactory();
        validator = factory.getValidator();
    }

    @Test
    void whenNameIsEmpty_thenValidationFails() {
        CreateStudentDTO dto = new CreateStudentDTO();
        dto.setName("");
        dto.setEmail("test@test.com");
        dto.setPassword("password123");

        Set<ConstraintViolation<CreateStudentDTO>> violations = validator.validate(dto);

        assertFalse(violations.isEmpty());
    }
}