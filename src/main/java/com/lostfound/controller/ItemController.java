package com.lostfound.controller;

import com.lostfound.entity.Item;
import com.lostfound.service.ItemService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import jakarta.validation.Valid;
import com.lostfound.dto.ItemDto;
import com.lostfound.dto.ItemStatusDto;

@RestController
@RequestMapping("/api/items")
public class ItemController {

    private final ItemService itemService;

    public ItemController(ItemService itemService) {
        this.itemService = itemService;
    }

    @GetMapping
    public Page<Item> list(@RequestParam(required = false) String type,
                           @RequestParam(required = false) String status,
                           @RequestParam(required = false) String q,
                           @RequestParam(defaultValue = "0") int page,
                           @RequestParam(defaultValue = "20") int size) {
        Pageable pageable = PageRequest.of(page, size);
        return itemService.list(type, status, q, pageable);
    }

    @GetMapping("/{id}")
    public ResponseEntity<Item> getOne(@PathVariable Long id) {
        return itemService.get(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @PostMapping
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<Item> create(@Valid @RequestBody ItemDto itemDto) {
        Item item = new Item();
        item.setTitle(itemDto.getTitle());
        item.setDescription(itemDto.getDescription());
        item.setType(itemDto.getType());
        item.setTags(itemDto.getTags());
        item.setLocation(itemDto.getLocation());
        Item created = itemService.create(item);
        return ResponseEntity.ok(created);
    }

    @PutMapping("/{id}")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<Item> update(@PathVariable Long id, @Valid @RequestBody ItemDto itemDto) {
        Item item = new Item();
        item.setTitle(itemDto.getTitle());
        item.setDescription(itemDto.getDescription());
        item.setType(itemDto.getType());
        item.setTags(itemDto.getTags());
        item.setLocation(itemDto.getLocation());
        Item updated = itemService.update(id, item);
        return ResponseEntity.ok(updated);
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        itemService.softDelete(id);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/my")
    @PreAuthorize("isAuthenticated()")
    public Page<Item> getMyItems(@RequestParam(required = false) String type,
                                  @RequestParam(required = false) String status,
                                  @RequestParam(required = false) String q,
                                  @RequestParam(defaultValue = "0") int page,
                                  @RequestParam(defaultValue = "20") int size) {
        Pageable pageable = PageRequest.of(page, size);
        return itemService.getMyItems(type, status, q, pageable);
    }

    @PatchMapping("/{id}/status")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<Item> updateStatus(@PathVariable Long id, 
                                             @Valid @RequestBody ItemStatusDto statusDto) {
        Item updated = itemService.updateStatus(id, statusDto.getStatus().name());
        return ResponseEntity.ok(updated);
    }
}


