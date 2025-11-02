package com.lostfound.service;

import com.lostfound.entity.Image;
import org.springframework.web.multipart.MultipartFile;

public interface ImageService {
    Image saveImage(MultipartFile file, Long itemId);
    Image getImageById(Long id);
    void deleteImage(Long id);
}


