package com.lostfound.controller;

import com.lostfound.entity.User;
import com.lostfound.repository.UserRepository;
import com.lostfound.repository.ClaimRequestRepository;
import com.lostfound.repository.ItemRepository;
import com.lostfound.repository.ReportRepository;
import com.lostfound.util.RoleNames;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/users")
public class UserController {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final ClaimRequestRepository claimRequestRepository;
    private final ItemRepository itemRepository;
    private final ReportRepository reportRepository;

    public UserController(UserRepository userRepository, PasswordEncoder passwordEncoder, ClaimRequestRepository claimRequestRepository, ItemRepository itemRepository, ReportRepository reportRepository) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.claimRequestRepository = claimRequestRepository;
        this.itemRepository = itemRepository;
        this.reportRepository = reportRepository;
    }

    @GetMapping
    @PreAuthorize("hasRole('ADMIN')")
    public List<User> listAll() {
        return userRepository.findAll().stream()
                .filter(user -> user.getRoles().stream()
                        .noneMatch(role -> role.getName().equals(RoleNames.ROLE_ADMIN)))
                .collect(Collectors.toList());
    }

    @GetMapping("/me")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<User> getCurrentUser() {
        User user = getCurrentAuthenticatedUser();
        return ResponseEntity.ok(user);
    }

    @PutMapping("/me")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<User> updateCurrentUser(@RequestBody Map<String, String> updates) {
        User user = getCurrentAuthenticatedUser();
        
        if (updates.containsKey("name")) {
            user.setName(updates.get("name"));
        }
        if (updates.containsKey("username")) {
            user.setUsername(updates.get("username"));
        }
        if (updates.containsKey("email")) {
            user.setEmail(updates.get("email"));
        }
        
        User updated = userRepository.save(user);
        return ResponseEntity.ok(updated);
    }

    @PutMapping("/me/password")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<String> changePassword(@RequestBody Map<String, String> passwords) {
        User user = getCurrentAuthenticatedUser();
        
        String currentPassword = passwords.get("currentPassword");
        String newPassword = passwords.get("newPassword");
        
        if (currentPassword == null || newPassword == null) {
            return ResponseEntity.badRequest().body("Current password and new password are required");
        }
        
        if (!passwordEncoder.matches(currentPassword, user.getPassword())) {
            return ResponseEntity.badRequest().body("Current password is incorrect");
        }
        
        user.setPassword(passwordEncoder.encode(newPassword));
        userRepository.save(user);
        
        return ResponseEntity.ok("Password changed successfully");
    }

    @GetMapping("/{id}")
    public ResponseEntity<User> getOne(@PathVariable Long id) {
        return userRepository.findById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    @Transactional
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        User user = userRepository.findById(id).orElse(null);
        if (user == null) {
            return ResponseEntity.notFound().build();
        }
        
        // Manually delete all related records before deleting user:
        // 1. Delete all claim requests by this user (as claimant)
        var claimRequests = claimRequestRepository.findByClaimant(user, org.springframework.data.domain.Pageable.unpaged());
        claimRequestRepository.deleteAll(claimRequests.getContent());
        
        // 2. Delete all items posted by this user
        // Note: Items may have softDeleted flag, so we need to find all (including soft deleted)
        var items = itemRepository.findByPostedBy(user);
        
        // 2a. Delete all reports for these items first (foreign key constraint)
        if (!items.isEmpty()) {
            var itemIds = items.stream().map(item -> item.getId()).toList();
            var reports = reportRepository.findByItemIds(itemIds);
            if (!reports.isEmpty()) {
                reportRepository.deleteAll(reports);
            }
        }
        
        // 2b. Now delete the items (Images will cascade via Item deletion)
        if (!items.isEmpty()) {
            itemRepository.deleteAll(items);
        }
        
        // 3. Now safe to delete the user
        userRepository.deleteById(id);
        return ResponseEntity.noContent().build();
    }

    private User getCurrentAuthenticatedUser() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String identity = authentication.getName();
        return userRepository.findByUsernameOrEmail(identity, identity)
                .orElseThrow(() -> new IllegalStateException("User not found"));
    }
}


