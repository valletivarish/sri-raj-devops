package com.lostfound.dto;

import com.lostfound.entity.Item.ItemType;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import jakarta.validation.ValidatorFactory;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

class ItemDtoTest {

    private static Validator validator;

    @BeforeAll
    static void setUp() {
        ValidatorFactory factory = Validation.buildDefaultValidatorFactory();
        validator = factory.getValidator();
    }

    @Test
    void testValidItemDto() {
        ItemDto dto = new ItemDto();
        dto.setTitle("Lost Wallet");
        dto.setDescription("Black leather wallet");
        dto.setType(ItemType.LOST);
        dto.setTags("wallet,black");
        dto.setLocation("Main Street");

        Set<ConstraintViolation<ItemDto>> violations = validator.validate(dto);
        assertTrue(violations.isEmpty(), "Valid ItemDto should not have violations");
    }

    @Test
    void testTitleRequired() {
        ItemDto dto = new ItemDto();
        dto.setTitle("");
        dto.setType(ItemType.LOST);
        dto.setDescription("Black leather wallet");
        dto.setTags("wallet,black");
        dto.setLocation("Main Street");

        Set<ConstraintViolation<ItemDto>> violations = validator.validate(dto);
        assertFalse(violations.isEmpty());
        assertTrue(violations.stream().anyMatch(v -> v.getMessage().equals("Title is required")));
    }

    @Test
    void testTitleTooLong() {
        ItemDto dto = new ItemDto();
        dto.setTitle("a".repeat(201));
        dto.setType(ItemType.LOST);
        dto.setDescription("Black leather wallet");
        dto.setTags("wallet,black");
        dto.setLocation("Main Street");

        Set<ConstraintViolation<ItemDto>> violations = validator.validate(dto);
        assertFalse(violations.isEmpty());
        assertTrue(violations.stream().anyMatch(v -> v.getMessage().contains("3 and 200 characters")));
    }

    @Test
    void testDescriptionTooLong() {
        ItemDto dto = new ItemDto();
        dto.setTitle("Test Item");
        dto.setDescription("a".repeat(2001));
        dto.setType(ItemType.LOST);
        dto.setTags("wallet,black");
        dto.setLocation("Main Street");

        Set<ConstraintViolation<ItemDto>> violations = validator.validate(dto);
        assertFalse(violations.isEmpty());
        assertTrue(violations.stream().anyMatch(v -> v.getMessage().contains("2000 characters")));
    }

    @Test
    void testDescriptionRequired() {
        ItemDto dto = new ItemDto();
        dto.setTitle("Test Item");
        dto.setDescription("   ");
        dto.setType(ItemType.LOST);
        dto.setTags("wallet,black");
        dto.setLocation("Main Street");

        Set<ConstraintViolation<ItemDto>> violations = validator.validate(dto);
        assertFalse(violations.isEmpty());
        assertTrue(violations.stream().anyMatch(v -> v.getMessage().equals("Description is required")));
    }

    @Test
    void testTypeRequired() {
        ItemDto dto = new ItemDto();
        dto.setTitle("Test Item");
        dto.setType(null);
        dto.setDescription("Black leather wallet");
        dto.setTags("wallet,black");
        dto.setLocation("Main Street");

        Set<ConstraintViolation<ItemDto>> violations = validator.validate(dto);
        assertFalse(violations.isEmpty());
        assertTrue(violations.stream().anyMatch(v -> v.getMessage().equals("Type is required")));
    }

    @Test
    void testTagsTooLong() {
        ItemDto dto = new ItemDto();
        dto.setTitle("Test Item");
        dto.setTags("a".repeat(501));
        dto.setType(ItemType.LOST);
        dto.setDescription("Black leather wallet");
        dto.setLocation("Main Street");

        Set<ConstraintViolation<ItemDto>> violations = validator.validate(dto);
        assertFalse(violations.isEmpty());
        assertTrue(violations.stream().anyMatch(v -> v.getMessage().contains("500 characters")));
    }

    @Test
    void testLocationTooLong() {
        ItemDto dto = new ItemDto();
        dto.setTitle("Test Item");
        dto.setLocation("a".repeat(201));
        dto.setType(ItemType.LOST);
        dto.setDescription("Black leather wallet");
        dto.setTags("wallet,black");

        Set<ConstraintViolation<ItemDto>> violations = validator.validate(dto);
        assertFalse(violations.isEmpty());
        assertTrue(violations.stream().anyMatch(v -> v.getMessage().contains("200 characters")));
    }

    @Test
    void testLocationRequired() {
        ItemDto dto = new ItemDto();
        dto.setTitle("Test Item");
        dto.setLocation("   ");
        dto.setType(ItemType.LOST);
        dto.setDescription("Black leather wallet");
        dto.setTags("wallet,black");

        Set<ConstraintViolation<ItemDto>> violations = validator.validate(dto);
        assertFalse(violations.isEmpty());
        assertTrue(violations.stream().anyMatch(v -> v.getMessage().equals("Location is required")));
    }

    @Test
    void testTrimWhitespace() {
        ItemDto dto = new ItemDto();
        dto.setTitle("  Lost Wallet  ");
        dto.setDescription("  Black leather wallet  ");
        dto.setTags("  wallet,black  ");
        dto.setLocation("  Main Street  ");
        dto.setType(ItemType.LOST);
        
        assertEquals("Lost Wallet", dto.getTitle());
        assertEquals("Black leather wallet", dto.getDescription());
        assertEquals("wallet,black", dto.getTags());
        assertEquals("Main Street", dto.getLocation());
    }
}

