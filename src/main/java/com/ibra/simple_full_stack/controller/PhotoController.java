package com.ibra.simple_full_stack.controller;

import com.ibra.simple_full_stack.dto.PhotoDto;
import com.ibra.simple_full_stack.dto.PhotoUploadRequest;
import com.ibra.simple_full_stack.service.PhotoService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.Map;

@Slf4j
@RestController
@RequestMapping("/api/photos")
@Tag(name = "Photo Management", description = "APIs for managing photo uploads and gallery")
@CrossOrigin(origins = "*", maxAge = 3600)
public class PhotoController {

    private final PhotoService photoService;

    public PhotoController(PhotoService photoService) {
        this.photoService = photoService;
    }

    @Operation(summary = "Upload a new photo", description = "Upload a photo file with description and metadata")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "Photo uploaded successfully"),
            @ApiResponse(responseCode = "400", description = "Invalid file or request data"),
            @ApiResponse(responseCode = "413", description = "File too large"),
            @ApiResponse(responseCode = "500", description = "Upload failed")
    })
    @PostMapping(value = "/upload", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<PhotoDto> uploadPhoto(
            @Parameter(description = "Image file to upload", required = true)
            @RequestParam("file") MultipartFile file,

            @Parameter(description = "Photo description", required = true)
            @RequestParam("description") String description,

            @Parameter(description = "Photo tags (comma-separated)")
            @RequestParam(value = "tags", required = false) String tags,

            @Parameter(description = "Photo location")
            @RequestParam(value = "location", required = false) String location,

            @Parameter(description = "Photo category")
            @RequestParam(value = "category", required = false) String category) {

        log.info("Received photo upload request for file: {}", file.getOriginalFilename());

        PhotoUploadRequest uploadRequest = PhotoUploadRequest.builder()
                .description(description)
                .tags(tags)
                .location(location)
                .category(category)
                .build();

        PhotoDto uploadedPhoto = photoService.uploadPhoto(file, uploadRequest);

        log.info("Successfully uploaded photo with ID: {}", uploadedPhoto.getId());
        return ResponseEntity.status(HttpStatus.CREATED).body(uploadedPhoto);
    }

    @Operation(summary = "Get all photos", description = "Retrieve all photos ordered by creation date (newest first)")
    @ApiResponse(responseCode = "200", description = "Photos retrieved successfully")
    @GetMapping
    public ResponseEntity<List<PhotoDto>> getAllPhotos() {
        log.debug("Fetching all photos");

        List<PhotoDto> photos = photoService.getAllPhotos();

        log.debug("Retrieved {} photos", photos.size());
        return ResponseEntity.ok(photos);
    }

    @Operation(summary = "Get photo by ID", description = "Retrieve a specific photo by its ID")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Photo found"),
            @ApiResponse(responseCode = "404", description = "Photo not found")
    })
    @GetMapping("/{id}")
    public ResponseEntity<PhotoDto> getPhotoById(
            @Parameter(description = "Photo ID", required = true)
            @PathVariable Long id) {

        log.debug("Fetching photo with ID: {}", id);

        PhotoDto photo = photoService.getPhotoById(id);
        return ResponseEntity.ok(photo);
    }

    @Operation(summary = "Update photo description", description = "Update the description of an existing photo")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Description updated successfully"),
            @ApiResponse(responseCode = "404", description = "Photo not found"),
            @ApiResponse(responseCode = "400", description = "Invalid description")
    })
    @PatchMapping("/{id}/description")
    public ResponseEntity<PhotoDto> updatePhotoDescription(
            @Parameter(description = "Photo ID", required = true)
            @PathVariable Long id,

            @Valid @RequestBody Map<String, String> request) {

        String description = request.get("description");
        if (description == null || description.trim().isEmpty()) {
            return ResponseEntity.badRequest().build();
        }

        log.info("Updating description for photo ID: {}", id);

        PhotoDto updatedPhoto = photoService.updatePhotoDescription(id, description.trim());
        return ResponseEntity.ok(updatedPhoto);
    }

    @Operation(summary = "Delete photo", description = "Delete a photo by its ID")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "204", description = "Photo deleted successfully"),
            @ApiResponse(responseCode = "404", description = "Photo not found"),
            @ApiResponse(responseCode = "500", description = "Delete failed")
    })
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deletePhoto(
            @Parameter(description = "Photo ID", required = true)
            @PathVariable Long id) {

        log.info("Deleting photo with ID: {}", id);

        boolean deleted = photoService.deletePhoto(id);
        if (deleted) {
            log.info("Successfully deleted photo with ID: {}", id);
            return ResponseEntity.noContent().build();
        } else {
            log.warn("Failed to delete photo with ID: {}", id);
            return ResponseEntity.notFound().build();
        }
    }

    @Operation(summary = "Refresh presigned URL", description = "Generate a new presigned URL for an existing photo")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "URL refreshed successfully"),
            @ApiResponse(responseCode = "404", description = "Photo not found"),
            @ApiResponse(responseCode = "500", description = "URL refresh failed")
    })
    @PatchMapping("/{id}/refresh-url")
    public ResponseEntity<PhotoDto> refreshPresignedUrl(
            @Parameter(description = "Photo ID", required = true)
            @PathVariable Long id) {

        log.info("Refreshing presigned URL for photo ID: {}", id);

        PhotoDto photo = photoService.refreshPresignedUrl(id);
        return ResponseEntity.ok(photo);
    }

    @Operation(summary = "Health check", description = "Check if the photo service is healthy")
    @ApiResponse(responseCode = "200", description = "Service is healthy")
    @GetMapping("/health")
    public ResponseEntity<Map<String, Object>> healthCheck() {
        Map<String, Object> health = Map.of(
                "status", "UP",
                "service", "PhotoService",
                "timestamp", System.currentTimeMillis()
        );

        return ResponseEntity.ok(health);
    }
}