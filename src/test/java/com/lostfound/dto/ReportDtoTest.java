package com.lostfound.dto;

import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import jakarta.validation.ValidatorFactory;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

class ReportDtoTest {

    private static Validator validator;

    @BeforeAll
    static void setUp() {
        ValidatorFactory factory = Validation.buildDefaultValidatorFactory();
        validator = factory.getValidator();
    }

    @Test
    void testValidReportDto() {
        ReportDto dto = new ReportDto();
        dto.setReporterContact("john@example.com");
        dto.setReason("This item is mine");

        Set<ConstraintViolation<ReportDto>> violations = validator.validate(dto);
        assertTrue(violations.isEmpty(), "Valid ReportDto should not have violations");
    }

    @Test
    void testReporterContactRequired() {
        ReportDto dto = new ReportDto();
        dto.setReporterContact("");
        dto.setReason("This item is mine");

        Set<ConstraintViolation<ReportDto>> violations = validator.validate(dto);
        assertFalse(violations.isEmpty());
        assertTrue(violations.stream().anyMatch(v -> v.getMessage().equals("Contact is required")));
    }

    @Test
    void testReporterContactTooShort() {
        ReportDto dto = new ReportDto();
        dto.setReporterContact("ab");
        dto.setReason("This item is mine");

        Set<ConstraintViolation<ReportDto>> violations = validator.validate(dto);
        assertFalse(violations.isEmpty());
        assertTrue(violations.stream().anyMatch(v -> v.getMessage().contains("3 and 100 characters")));
    }

    @Test
    void testReporterContactTooLong() {
        ReportDto dto = new ReportDto();
        dto.setReporterContact("a".repeat(101));
        dto.setReason("This item is mine");

        Set<ConstraintViolation<ReportDto>> violations = validator.validate(dto);
        assertFalse(violations.isEmpty());
        assertTrue(violations.stream().anyMatch(v -> v.getMessage().contains("3 and 100 characters")));
    }

    @Test
    void testReporterContactInvalidCharacters() {
        ReportDto dto = new ReportDto();
        dto.setReporterContact("john@example.com<script>");
        dto.setReason("This item is mine");

        Set<ConstraintViolation<ReportDto>> violations = validator.validate(dto);
        assertFalse(violations.isEmpty());
        assertTrue(violations.stream().anyMatch(v -> v.getMessage().contains("invalid characters")));
    }

    @Test
    void testReasonTooLong() {
        ReportDto dto = new ReportDto();
        dto.setReporterContact("john@example.com");
        dto.setReason("a".repeat(1001));

        Set<ConstraintViolation<ReportDto>> violations = validator.validate(dto);
        assertFalse(violations.isEmpty());
        assertTrue(violations.stream().anyMatch(v -> v.getMessage().contains("1000 characters")));
    }

    @Test
    void testTrimWhitespace() {
        ReportDto dto = new ReportDto();
        dto.setReporterContact("  john@example.com  ");
        dto.setReason("  This item is mine  ");
        
        assertEquals("john@example.com", dto.getReporterContact());
        assertEquals("This item is mine", dto.getReason());
    }
}

