package com.ibra.simple_full_stack.service.impl;

import com.ibra.simple_full_stack.dto.PhotoDto;
import com.ibra.simple_full_stack.dto.PhotoUploadRequest;
import com.ibra.simple_full_stack.exception.PhotoNotFoundException;
import com.ibra.simple_full_stack.exception.PhotoUploadException;
import com.ibra.simple_full_stack.mapper.PhotoMapper;
import com.ibra.simple_full_stack.model.Photo;
import com.ibra.simple_full_stack.repository.PhotoRepository;
import com.ibra.simple_full_stack.service.PhotoService;
import com.ibra.simple_full_stack.service.S3Service;
import com.ibra.simple_full_stack.util.FileUtils;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Slf4j
@Service
@Transactional
public class PhotoServiceImpl implements PhotoService {

    private final PhotoRepository photoRepository;
    private final S3Service s3Service;
    private final PhotoMapper photoMapper;

    private static final int PRESIGNED_URL_DURATION_DAYS = 3;
    private static final long MAX_FILE_SIZE = 10 * 1024 * 1024; // 10MB
    private static final List<String> ALLOWED_CONTENT_TYPES = List.of(
            "image/jpeg", "image/jpg", "image/png", "image/gif", "image/webp"
    );

    public PhotoServiceImpl(PhotoRepository photoRepository, S3Service s3Service, PhotoMapper photoMapper) {
        this.photoRepository = photoRepository;
        this.s3Service = s3Service;
        this.photoMapper = photoMapper;
    }

    @Override
    public PhotoDto uploadPhoto(MultipartFile file, PhotoUploadRequest uploadRequest) {
        log.info("Starting photo upload process for file: {}", file.getOriginalFilename());

        // Validate file
        validateFile(file);

        try {
            // Generate unique filename
            String uniqueFileName = s3Service.generateUniqueFileName(file.getOriginalFilename());

            // Upload to S3
            boolean uploadSuccess = s3Service.uploadFile(file, uniqueFileName);
            if (!uploadSuccess) {
                throw new PhotoUploadException("Failed to upload file to S3");
            }

            // Generate presigned URL
            String presignedUrl = s3Service.generatePresignedUrl(uniqueFileName, PRESIGNED_URL_DURATION_DAYS);
            if (presignedUrl == null) {
                // Cleanup uploaded file
                s3Service.deleteFile(uniqueFileName);
                throw new PhotoUploadException("Failed to generate presigned URL");
            }

            // Create and save photo entity
            Photo photo = Photo.builder()
                    .fileName(uniqueFileName)
                    .originalFileName(file.getOriginalFilename())
                    .description(uploadRequest.getDescription())
                    .presignedUrl(presignedUrl)
                    .fileSize(file.getSize())
                    .contentType(file.getContentType())
                    .tags(uploadRequest.getTags())
                    .location(uploadRequest.getLocation())
                    .category(uploadRequest.getCategory())
                    .build();

            photo = photoRepository.save(photo);

            log.info("Successfully uploaded photo with ID: {}", photo.getId());
            return photoMapper.convertToDto(photo);

        } catch (Exception e) {
            log.error("Error during photo upload: {}", e.getMessage(), e);
            throw new PhotoUploadException("Failed to upload photo: " + e.getMessage());
        }
    }

    @Override
    @Transactional(readOnly = true)
    public List<PhotoDto> getAllPhotos() {
        log.debug("Fetching all photos from database");

        List<Photo> photos = photoRepository.findAllByOrderByCreatedAtDesc();
        List<PhotoDto> photoDtos = photos.stream()
                .map(photoMapper::convertToDto)
                .collect(Collectors.toList());

        log.debug("Retrieved {} photos from database", photoDtos.size());
        return photoDtos;
    }

    @Override
    @Transactional(readOnly = true)
    public PhotoDto getPhotoById(Long id) {
        log.debug("Fetching photo with ID: {}", id);

        Optional<Photo> photo = photoRepository.findById(id);
        if (photo.isEmpty()) {
            log.warn("Photo not found with ID: {}", id);
            throw new PhotoNotFoundException("Photo not found with ID: " + id);
        }

        return photoMapper.convertToDto(photo.get());
    }

    @Override
    public boolean deletePhoto(Long id) {
        log.info("Deleting photo with ID: {}", id);

        Optional<Photo> photoOpt = photoRepository.findById(id);
        if (photoOpt.isEmpty()) {
            log.warn("Photo not found with ID: {}", id);
            return false;
        }

        Photo photo = photoOpt.get();

        try {
            // Delete from S3
            boolean s3DeleteSuccess = s3Service.deleteFile(photo.getFileName());
            if (!s3DeleteSuccess) {
                log.warn("Failed to delete file from S3: {}", photo.getFileName());
            }

            // Delete from database
            photoRepository.delete(photo);

            log.info("Successfully deleted photo with ID: {}", id);
            return true;

        } catch (Exception e) {
            log.error("Error deleting photo with ID {}: {}", id, e.getMessage(), e);
            return false;
        }
    }

    @Override
    public PhotoDto updatePhotoDescription(Long id, String description) {
        log.info("Updating description for photo with ID: {}", id);

        Optional<Photo> photoOpt = photoRepository.findById(id);
        if (photoOpt.isEmpty()) {
            log.warn("Photo not found with ID: {}", id);
            throw new PhotoNotFoundException("Photo not found with ID: " + id);
        }

        Photo photo = photoOpt.get();
        photo.setDescription(description);
        photo = photoRepository.save(photo);

        log.info("Successfully updated description for photo with ID: {}", id);
        return photoMapper.convertToDto(photo);
    }

    @Override
    public PhotoDto refreshPresignedUrl(Long id) {
        log.info("Refreshing presigned URL for photo with ID: {}", id);

        Optional<Photo> photoOpt = photoRepository.findById(id);
        if (photoOpt.isEmpty()) {
            log.warn("Photo not found with ID: {}", id);
            throw new PhotoNotFoundException("Photo not found with ID: " + id);
        }

        Photo photo = photoOpt.get();

        // Check if file still exists in S3
        if (!s3Service.doesFileExist(photo.getFileName())) {
            log.error("File does not exist in S3: {}", photo.getFileName());
            throw new PhotoNotFoundException("Photo file not found in storage");
        }

        // Generate new presigned URL
        String newPresignedUrl = s3Service.generatePresignedUrl(photo.getFileName(), PRESIGNED_URL_DURATION_DAYS);
        if (newPresignedUrl == null) {
            throw new PhotoUploadException("Failed to generate new presigned URL");
        }

        photo.setPresignedUrl(newPresignedUrl);
        photo = photoRepository.save(photo);

        log.info("Successfully refreshed presigned URL for photo with ID: {}", id);
        return photoMapper.convertToDto(photo);
    }

    private void validateFile(MultipartFile file) {
        if (file == null || file.isEmpty()) {
            throw new PhotoUploadException("File is required");
        }

        if (file.getSize() > MAX_FILE_SIZE) {
            throw new PhotoUploadException("File size exceeds maximum allowed size of " +
                    FileUtils.formatFileSize(MAX_FILE_SIZE));
        }

        String contentType = file.getContentType();
        if (contentType == null || !ALLOWED_CONTENT_TYPES.contains(contentType.toLowerCase())) {
            throw new PhotoUploadException("Invalid file type. Allowed types: " + ALLOWED_CONTENT_TYPES);
        }

        if (file.getOriginalFilename() == null || file.getOriginalFilename().trim().isEmpty()) {
            throw new PhotoUploadException("File must have a valid filename");
        }
    }
}