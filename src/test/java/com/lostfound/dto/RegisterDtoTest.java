package com.lostfound.dto;

import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import jakarta.validation.ValidatorFactory;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

class RegisterDtoTest {

    private static Validator validator;

    @BeforeAll
    static void setUp() {
        ValidatorFactory factory = Validation.buildDefaultValidatorFactory();
        validator = factory.getValidator();
    }

    @Test
    void testValidRegisterDto() {
        RegisterDto dto = new RegisterDto();
        dto.setName("John Doe");
        dto.setUsername("johndoe");
        dto.setEmail("john@example.com");
        dto.setPassword("password123");

        Set<ConstraintViolation<RegisterDto>> violations = validator.validate(dto);
        assertTrue(violations.isEmpty(), "Valid RegisterDto should not have violations");
    }

    @Test
    void testNameRequired() {
        RegisterDto dto = new RegisterDto();
        dto.setName("");
        dto.setUsername("johndoe");
        dto.setEmail("john@example.com");
        dto.setPassword("password123");

        Set<ConstraintViolation<RegisterDto>> violations = validator.validate(dto);
        assertFalse(violations.isEmpty());
        assertTrue(violations.stream().anyMatch(v -> v.getMessage().equals("Name is required")));
    }

    @Test
    void testNameTooShort() {
        RegisterDto dto = new RegisterDto();
        dto.setName("J");
        dto.setUsername("johndoe");
        dto.setEmail("john@example.com");
        dto.setPassword("password123");

        Set<ConstraintViolation<RegisterDto>> violations = validator.validate(dto);
        assertFalse(violations.isEmpty());
        assertTrue(violations.stream().anyMatch(v -> v.getMessage().contains("2 and 100 characters")));
    }

    @Test
    void testUsernameRequired() {
        RegisterDto dto = new RegisterDto();
        dto.setName("John Doe");
        dto.setUsername("");
        dto.setEmail("john@example.com");
        dto.setPassword("password123");

        Set<ConstraintViolation<RegisterDto>> violations = validator.validate(dto);
        assertFalse(violations.isEmpty());
        assertTrue(violations.stream().anyMatch(v -> v.getMessage().equals("Username is required")));
    }

    @Test
    void testUsernameInvalidCharacters() {
        RegisterDto dto = new RegisterDto();
        dto.setName("John Doe");
        dto.setUsername("john@doe");
        dto.setEmail("john@example.com");
        dto.setPassword("password123");

        Set<ConstraintViolation<RegisterDto>> violations = validator.validate(dto);
        assertFalse(violations.isEmpty());
        assertTrue(violations.stream().anyMatch(v -> v.getMessage().contains("letters, numbers, underscores, and hyphens")));
    }

    @Test
    void testUsernameTooShort() {
        RegisterDto dto = new RegisterDto();
        dto.setName("John Doe");
        dto.setUsername("ab");
        dto.setEmail("john@example.com");
        dto.setPassword("password123");

        Set<ConstraintViolation<RegisterDto>> violations = validator.validate(dto);
        assertFalse(violations.isEmpty());
        assertTrue(violations.stream().anyMatch(v -> v.getMessage().contains("3 and 50 characters")));
    }

    @Test
    void testEmailRequired() {
        RegisterDto dto = new RegisterDto();
        dto.setName("John Doe");
        dto.setUsername("johndoe");
        dto.setEmail("");
        dto.setPassword("password123");

        Set<ConstraintViolation<RegisterDto>> violations = validator.validate(dto);
        assertFalse(violations.isEmpty());
        assertTrue(violations.stream().anyMatch(v -> v.getMessage().equals("Email is required")));
    }

    @Test
    void testEmailInvalidFormat() {
        RegisterDto dto = new RegisterDto();
        dto.setName("John Doe");
        dto.setUsername("johndoe");
        dto.setEmail("invalid-email");
        dto.setPassword("password123");

        Set<ConstraintViolation<RegisterDto>> violations = validator.validate(dto);
        assertFalse(violations.isEmpty());
        assertTrue(violations.stream().anyMatch(v -> v.getMessage().equals("Email must be a valid email address")));
    }

    @Test
    void testEmailNormalizedToLowercase() {
        RegisterDto dto = new RegisterDto();
        dto.setEmail("JOHN@EXAMPLE.COM");
        
        assertEquals("john@example.com", dto.getEmail());
    }

    @Test
    void testTrimWhitespace() {
        RegisterDto dto = new RegisterDto();
        dto.setName("  John Doe  ");
        dto.setUsername("  johndoe  ");
        dto.setEmail("  john@example.com  ");
        
        assertEquals("John Doe", dto.getName());
        assertEquals("johndoe", dto.getUsername());
        assertEquals("john@example.com", dto.getEmail());
    }
}

