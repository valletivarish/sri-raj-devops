package com.lostfound.service;

import com.lostfound.entity.ClaimRequest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface ClaimRequestService {
    ClaimRequest create(Long itemId, String message);
    Page<ClaimRequest> listForItem(Long itemId, Pageable pageable);
    Page<ClaimRequest> listMine(Pageable pageable);
    ClaimRequest approve(Long itemId, Long requestId);
    ClaimRequest reject(Long itemId, Long requestId);
}



