package com.lostfound.service.impl;

import com.lostfound.entity.ClaimRequest;
import com.lostfound.entity.Item;
import com.lostfound.entity.User;
import com.lostfound.repository.ClaimRequestRepository;
import com.lostfound.repository.ItemRepository;
import com.lostfound.repository.UserRepository;
import com.lostfound.service.ClaimRequestService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Set;
import java.util.stream.Collectors;

@Service
@Transactional
public class ClaimRequestServiceImpl implements ClaimRequestService {

    private final ClaimRequestRepository claimRequestRepository;
    private final ItemRepository itemRepository;
    private final UserRepository userRepository;

    public ClaimRequestServiceImpl(ClaimRequestRepository claimRequestRepository,
                                   ItemRepository itemRepository,
                                   UserRepository userRepository) {
        this.claimRequestRepository = claimRequestRepository;
        this.itemRepository = itemRepository;
        this.userRepository = userRepository;
    }

    @Override
    public ClaimRequest create(Long itemId, String message) {
        // Fetch fresh item from database to ensure we have latest status
        Item item = itemRepository.findById(itemId)
                .orElseThrow(() -> new IllegalArgumentException("Item not found"));
        
        // Validate item is not soft deleted
        if (item.isSoftDeleted()) {
            throw new IllegalStateException("Item is deleted");
        }
        
        // Validate item is not already claimed - check enum comparison explicitly
        Item.ItemStatus currentStatus = item.getStatus();
        if (currentStatus == null || currentStatus.equals(Item.ItemStatus.CLAIMED)) {
            throw new IllegalStateException("Item is already claimed");
        }
        
        // Validate item is not removed
        if (currentStatus.equals(Item.ItemStatus.REMOVED)) {
            throw new IllegalStateException("Item has been removed");
        }
        
        User me = currentUser();
        
        // Check if user already has a pending claim request for this item
        var existingRequests = claimRequestRepository.findByItem(item, org.springframework.data.domain.Pageable.unpaged());
        boolean hasPendingRequest = existingRequests.getContent().stream()
                .anyMatch(cr -> cr.getClaimant() != null && 
                               cr.getClaimant().getId() != null &&
                               cr.getClaimant().getId().equals(me.getId()) && 
                               cr.getStatus() == ClaimRequest.Status.PENDING);
        
        if (hasPendingRequest) {
            throw new IllegalStateException("You already have a pending claim request for this item");
        }
        
        ClaimRequest cr = new ClaimRequest();
        cr.setItem(item);
        cr.setClaimant(me);
        cr.setMessage(message);
        return claimRequestRepository.save(cr);
    }

    @Override
    public Page<ClaimRequest> listForItem(Long itemId, Pageable pageable) {
        Item item = itemRepository.findById(itemId).orElseThrow(() -> new IllegalArgumentException("Item not found"));
        requireOwnerOrAdmin(item);
        return claimRequestRepository.findByItem(item, pageable);
    }

    @Override
    public Page<ClaimRequest> listMine(Pageable pageable) {
        return claimRequestRepository.findByClaimant(currentUser(), pageable);
    }

    @Override
    public ClaimRequest approve(Long itemId, Long requestId) {
        Item item = itemRepository.findById(itemId).orElseThrow(() -> new IllegalArgumentException("Item not found"));
        requireOwnerOrAdmin(item);
        ClaimRequest cr = claimRequestRepository.findById(requestId).orElseThrow(() -> new IllegalArgumentException("Request not found"));
        if (!cr.getItem().getId().equals(itemId)) {
            throw new IllegalArgumentException("Request does not belong to item");
        }
        cr.setStatus(ClaimRequest.Status.APPROVED);
        item.setStatus(Item.ItemStatus.CLAIMED);
        itemRepository.save(item);
        return claimRequestRepository.save(cr);
    }

    @Override
    public ClaimRequest reject(Long itemId, Long requestId) {
        Item item = itemRepository.findById(itemId).orElseThrow(() -> new IllegalArgumentException("Item not found"));
        requireOwnerOrAdmin(item);
        ClaimRequest cr = claimRequestRepository.findById(requestId).orElseThrow(() -> new IllegalArgumentException("Request not found"));
        if (!cr.getItem().getId().equals(itemId)) {
            throw new IllegalArgumentException("Request does not belong to item");
        }
        cr.setStatus(ClaimRequest.Status.REJECTED);
        return claimRequestRepository.save(cr);
    }

    private User currentUser() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        String id = auth.getName();
        return userRepository.findByUsernameOrEmail(id, id)
                .orElseThrow(() -> new IllegalStateException("User not found"));
    }

    private void requireOwnerOrAdmin(Item item) {
        if (isAdmin()) return;
        User me = currentUser();
        if (!item.getPostedBy().getId().equals(me.getId())) {
            throw new SecurityException("Not allowed");
        }
    }

    private boolean isAdmin() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        Set<String> roles = auth.getAuthorities().stream().map(GrantedAuthority::getAuthority).collect(Collectors.toSet());
        return roles.contains("ROLE_ADMIN");
    }
}


