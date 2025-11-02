package com.lostfound.service.impl;

import com.lostfound.entity.Image;
import com.lostfound.entity.Item;
import com.lostfound.entity.Item.ItemStatus;
import com.lostfound.entity.Item.ItemType;
import com.lostfound.entity.User;
import com.lostfound.repository.ImageRepository;
import com.lostfound.repository.ItemRepository;
import com.lostfound.repository.UserRepository;
import com.lostfound.service.ItemService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Locale;
import java.util.Optional;

@Service
@Transactional
public class ItemServiceImpl implements ItemService {

    private final ItemRepository itemRepository;
    private final UserRepository userRepository;
    private final ImageRepository imageRepository;

    public ItemServiceImpl(ItemRepository itemRepository, UserRepository userRepository, ImageRepository imageRepository) {
        this.itemRepository = itemRepository;
        this.userRepository = userRepository;
        this.imageRepository = imageRepository;
    }

    @Override
    public Page<Item> list(String type, String status, String q, Pageable pageable) {
        ItemType t = null;
        if (type != null && !type.isBlank()) {
            t = ItemType.valueOf(type.trim().toUpperCase(Locale.ROOT));
        }
        ItemStatus s = null;
        if (status != null && !status.isBlank()) {
            s = ItemStatus.valueOf(status.trim().toUpperCase(Locale.ROOT));
        }
        String query = (q == null || q.isBlank()) ? null : q.trim();
        return itemRepository.search(t, s, query, pageable);
    }

    @Override
    public Item create(Item item) {
        item.setId(null);
        item.setSoftDeleted(false);
        item.setStatus(ItemStatus.OPEN);
        item.setPostedBy(getCurrentUser());
        item.setImages(new java.util.ArrayList<>());
        Item savedItem = itemRepository.save(item);
        
        return savedItem;
    }

    @Override
    public Optional<Item> get(Long id) {
        return itemRepository.findByIdWithImages(id);
    }

    @Override
    public Item update(Long id, Item payload) {
        Item existing = itemRepository.findById(id).orElseThrow(() -> new IllegalArgumentException("Item not found"));
        if (existing.isSoftDeleted()) {
            throw new IllegalStateException("Item is deleted");
        }
        if (payload.getTitle() != null) existing.setTitle(payload.getTitle());
        if (payload.getDescription() != null) existing.setDescription(payload.getDescription());
        if (payload.getType() != null) existing.setType(payload.getType());
        if (payload.getTags() != null) existing.setTags(payload.getTags());
        if (payload.getLocation() != null) existing.setLocation(payload.getLocation());
        if (payload.getStatus() != null) existing.setStatus(payload.getStatus());
        
        if (payload.getImages() != null && !payload.getImages().isEmpty()) {
            for (Image imageRef : payload.getImages()) {
                if (imageRef.getId() != null) {
                    Image image = imageRepository.findById(imageRef.getId())
                            .orElseThrow(() -> new IllegalArgumentException("Image not found: " + imageRef.getId()));
                    image.setItem(existing);
                    imageRepository.save(image);
                }
            }
        }
        
        Item savedItem = itemRepository.save(existing);
        savedItem = itemRepository.findByIdWithImages(savedItem.getId()).orElse(savedItem);
        return savedItem;
    }

    @Override
    public void softDelete(Long id) {
        Item existing = itemRepository.findById(id).orElseThrow(() -> new IllegalArgumentException("Item not found"));
        existing.setSoftDeleted(true);
        itemRepository.save(existing);
    }

    @Override
    public Page<Item> getMyItems(String type, String status, String q, Pageable pageable) {
        User currentUser = getCurrentUser();
        ItemType t = null;
        if (type != null && !type.isBlank()) {
            t = ItemType.valueOf(type.trim().toUpperCase(Locale.ROOT));
        }
        ItemStatus s = null;
        if (status != null && !status.isBlank()) {
            s = ItemStatus.valueOf(status.trim().toUpperCase(Locale.ROOT));
        }
        String query = (q == null || q.isBlank()) ? null : q.trim();
        return itemRepository.searchByPostedBy(currentUser, t, s, query, pageable);
    }

    @Override
    public Item updateStatus(Long id, String statusStr) {
        Item existing = itemRepository.findById(id).orElseThrow(() -> new IllegalArgumentException("Item not found"));
        if (existing.isSoftDeleted()) {
            throw new IllegalStateException("Item is deleted");
        }
        ItemStatus status = ItemStatus.valueOf(statusStr.trim().toUpperCase(Locale.ROOT));
        existing.setStatus(status);
        return itemRepository.save(existing);
    }

    private User getCurrentUser() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String identity = authentication.getName();
        return userRepository.findByUsernameOrEmail(identity, identity)
                .orElseThrow(() -> new IllegalStateException("User not found"));
    }
}


