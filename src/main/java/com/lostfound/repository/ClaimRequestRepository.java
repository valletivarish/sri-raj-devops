package com.lostfound.repository;

import com.lostfound.entity.ClaimRequest;
import com.lostfound.entity.Item;
import com.lostfound.entity.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ClaimRequestRepository extends JpaRepository<ClaimRequest, Long> {
    Page<ClaimRequest> findByItem(Item item, Pageable pageable);
    Page<ClaimRequest> findByClaimant(User claimant, Pageable pageable);
}



