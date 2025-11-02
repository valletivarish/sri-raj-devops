package com.lostfound.controller;

import com.lostfound.entity.ClaimRequest;
import com.lostfound.service.ClaimRequestService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/items/{itemId}/claim-requests")
public class ClaimRequestController {

    private final ClaimRequestService claimRequestService;

    public ClaimRequestController(ClaimRequestService claimRequestService) {
        this.claimRequestService = claimRequestService;
    }

    @PostMapping
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<ClaimRequest> create(@PathVariable Long itemId, @RequestBody Map<String, String> body) {
        String message = body == null ? null : body.get("message");
        return ResponseEntity.ok(claimRequestService.create(itemId, message));
    }

    @GetMapping
    @PreAuthorize("isAuthenticated()")
    public Page<ClaimRequest> list(@PathVariable Long itemId,
                                   @RequestParam(defaultValue = "0") int page,
                                   @RequestParam(defaultValue = "20") int size) {
        Pageable pageable = PageRequest.of(page, size);
        return claimRequestService.listForItem(itemId, pageable);
    }

    @PostMapping("/{requestId}/approve")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<ClaimRequest> approve(@PathVariable Long itemId, @PathVariable Long requestId) {
        return ResponseEntity.ok(claimRequestService.approve(itemId, requestId));
    }

    @PostMapping("/{requestId}/reject")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<ClaimRequest> reject(@PathVariable Long itemId, @PathVariable Long requestId) {
        return ResponseEntity.ok(claimRequestService.reject(itemId, requestId));
    }
}



