package com.lostfound.service.impl;

import com.lostfound.entity.Image;
import com.lostfound.entity.Item;
import com.lostfound.repository.ImageRepository;
import com.lostfound.repository.ItemRepository;
import com.lostfound.service.ImageService;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;

@Service
public class ImageServiceImpl implements ImageService {

    private final ImageRepository imageRepository;
    private final ItemRepository itemRepository;

    public ImageServiceImpl(ImageRepository imageRepository, ItemRepository itemRepository) {
        this.imageRepository = imageRepository;
        this.itemRepository = itemRepository;
    }

    @Override
    public Image saveImage(MultipartFile file, Long itemId) {
        try {
            Image image = new Image();
            image.setData(file.getBytes());
            image.setContentType(file.getContentType() != null && !file.getContentType().isEmpty() ? file.getContentType() : "image/jpeg");
            String original = StringUtils.cleanPath(file.getOriginalFilename() != null && !file.getOriginalFilename().isEmpty() ? file.getOriginalFilename() : "image");
            image.setFilename(original);
            
            if (itemId != null) {
                Item item = itemRepository.findById(itemId)
                        .orElseThrow(() -> new IllegalArgumentException("Item not found: " + itemId));
                image.setItem(item);
            }
            
            return imageRepository.save(image);
        } catch (IOException e) {
            throw new IllegalStateException("Failed to read image data", e);
        }
    }

    @Override
    public Image getImageById(Long id) {
        return imageRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Image not found"));
    }

    @Override
    public void deleteImage(Long id) {
        imageRepository.deleteById(id);
    }
}


