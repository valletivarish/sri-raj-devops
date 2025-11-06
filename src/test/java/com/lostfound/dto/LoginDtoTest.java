package com.lostfound.dto;

import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import jakarta.validation.ValidatorFactory;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

class LoginDtoTest {

    private static Validator validator;

    @BeforeAll
    static void setUp() {
        ValidatorFactory factory = Validation.buildDefaultValidatorFactory();
        validator = factory.getValidator();
    }

    @Test
    void testValidLoginDto() {
        LoginDto dto = new LoginDto();
        dto.setUsernameOrEmail("testuser");
        dto.setPassword("password123");

        Set<ConstraintViolation<LoginDto>> violations = validator.validate(dto);
        assertTrue(violations.isEmpty(), "Valid LoginDto should not have violations");
    }

    @Test
    void testUsernameOrEmailRequired() {
        LoginDto dto = new LoginDto();
        dto.setUsernameOrEmail("");
        dto.setPassword("password123");

        Set<ConstraintViolation<LoginDto>> violations = validator.validate(dto);
        assertFalse(violations.isEmpty(), "Expected violations for blank username or email");
        assertTrue(violations.stream().anyMatch(v -> v.getMessage().equals("Username or email is required")));
    }

    @Test
    void testUsernameOrEmailTooShort() {
        LoginDto dto = new LoginDto();
        dto.setUsernameOrEmail("ab");
        dto.setPassword("password123");

        Set<ConstraintViolation<LoginDto>> violations = validator.validate(dto);
        assertFalse(violations.isEmpty());
        assertTrue(violations.stream().anyMatch(v -> v.getMessage().contains("3 and 100 characters")));
    }

    @Test
    void testUsernameOrEmailTooLong() {
        LoginDto dto = new LoginDto();
        dto.setUsernameOrEmail("a".repeat(101));
        dto.setPassword("password123");

        Set<ConstraintViolation<LoginDto>> violations = validator.validate(dto);
        assertFalse(violations.isEmpty());
        assertTrue(violations.stream().anyMatch(v -> v.getMessage().contains("3 and 100 characters")));
    }

    @Test
    void testPasswordRequired() {
        LoginDto dto = new LoginDto();
        dto.setUsernameOrEmail("testuser");
        dto.setPassword("");

        Set<ConstraintViolation<LoginDto>> violations = validator.validate(dto);
        assertFalse(violations.isEmpty(), "Expected violations for blank password");
        assertTrue(violations.stream().anyMatch(v -> v.getMessage().equals("Password is required")));
    }

    @Test
    void testPasswordTooShort() {
        LoginDto dto = new LoginDto();
        dto.setUsernameOrEmail("testuser");
        dto.setPassword("pass");

        Set<ConstraintViolation<LoginDto>> violations = validator.validate(dto);
        assertFalse(violations.isEmpty());
        assertTrue(violations.stream().anyMatch(v -> v.getMessage().contains("6 and 100 characters")));
    }

    @Test
    void testPasswordTooLong() {
        LoginDto dto = new LoginDto();
        dto.setUsernameOrEmail("testuser");
        dto.setPassword("a".repeat(101));

        Set<ConstraintViolation<LoginDto>> violations = validator.validate(dto);
        assertFalse(violations.isEmpty());
        assertTrue(violations.stream().anyMatch(v -> v.getMessage().contains("6 and 100 characters")));
    }

    @Test
    void testTrimWhitespace() {
        LoginDto dto = new LoginDto();
        dto.setUsernameOrEmail("  testuser  ");
        
        assertEquals("testuser", dto.getUsernameOrEmail());
    }
}

