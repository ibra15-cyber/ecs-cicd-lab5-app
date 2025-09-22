package com.ibra.simple_full_stack.service;

import com.ibra.simple_full_stack.dto.PhotoDto;
import com.ibra.simple_full_stack.dto.PhotoUploadRequest;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

public interface PhotoService {
    
    /**
     * Upload a photo to S3 and save metadata to database
     * @param file The uploaded file
     * @param uploadRequest Upload request containing description and other metadata
     * @return PhotoDto with presigned URL and metadata
     */
    PhotoDto uploadPhoto(MultipartFile file, PhotoUploadRequest uploadRequest);
    
    /**
     * Get all photos ordered by creation date descending
     * @return List of PhotoDto objects
     */
    List<PhotoDto> getAllPhotos();
    
    /**
     * Get a specific photo by ID
     * @param id Photo ID
     * @return PhotoDto or null if not found
     */
    PhotoDto getPhotoById(Long id);
    
    /**
     * Delete a photo by ID
     * @param id Photo ID
     * @return true if deleted successfully, false otherwise
     */
    boolean deletePhoto(Long id);
    
    /**
     * Update photo description
     * @param id Photo ID
     * @param description New description
     * @return Updated PhotoDto or null if not found
     */
    PhotoDto updatePhotoDescription(Long id, String description);
    
    /**
     * Generate new presigned URL for existing photo
     * @param id Photo ID
     * @return PhotoDto with new presigned URL
     */
    PhotoDto refreshPresignedUrl(Long id);
}