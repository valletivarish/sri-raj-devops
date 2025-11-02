package com.lostfound.service;

import com.lostfound.entity.Item;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.Optional;

public interface ItemService {
    Page<Item> list(String type, String status, String q, Pageable pageable);
    Item create(Item item);
    Optional<Item> get(Long id);
    Item update(Long id, Item item);
    void softDelete(Long id);
    Page<Item> getMyItems(String type, String status, String q, Pageable pageable);
    Item updateStatus(Long id, String status);
}


