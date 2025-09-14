package com.ibra.simple_full_stack.controller;

import com.ibra.simple_full_stack.model.Photo;
import com.ibra.simple_full_stack.repository.PhotoRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;
import software.amazon.awssdk.services.s3.presigner.S3Presigner;
import software.amazon.awssdk.services.s3.presigner.model.GetObjectPresignRequest;
import software.amazon.awssdk.services.s3.presigner.model.PresignedGetObjectRequest;
import software.amazon.awssdk.core.sync.RequestBody;

import java.io.IOException;
import java.time.Duration;
import java.util.Date;
import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/photos")
public class PhotoController {
    
    private final PhotoRepository photoRepository;
    
    private final S3Client s3Client;
    
    private final S3Presigner s3Presigner;
    
    @Value("${s3.bucket.name}")
    private String bucketName;

    public PhotoController(PhotoRepository photoRepository, S3Client s3Client, S3Presigner s3Presigner) {
        this.photoRepository = photoRepository;
        this.s3Client = s3Client;
        this.s3Presigner = s3Presigner;
    }

    @PostMapping("/upload")
    public ResponseEntity<Photo> uploadPhoto(
            @RequestParam("file") MultipartFile file,
            @RequestParam("description") String description) {
        
        try {
            // Generate unique file name
            String fileName = UUID.randomUUID().toString() + "_" + file.getOriginalFilename();
            
            // Upload to S3
            s3Client.putObject(PutObjectRequest.builder()
                    .bucket(bucketName)
                    .key(fileName)
                    .build(),
                RequestBody.fromBytes(file.getBytes()));
            
            // Generate presigned URL (valid for 3 days)
            PresignedGetObjectRequest presignedRequest = s3Presigner.presignGetObject(
                GetObjectPresignRequest.builder()
                    .getObjectRequest(b -> b.bucket(bucketName).key(fileName))
                    .signatureDuration(Duration.ofDays(3))
                    .build());
            
            String presignedUrl = presignedRequest.url().toString();
            
            // Save metadata to database
            Photo photo = new Photo();
            photo.setFileName(fileName);
            photo.setDescription(description);
            photo.setPresignedUrl(presignedUrl);
            photo.setCreatedAt(new Date());
            
            photoRepository.save(photo);
            
            return ResponseEntity.ok(photo);
        } catch (IOException e) {
            return ResponseEntity.status(500).build();
        }
    }
    
    @GetMapping
    public ResponseEntity<List<Photo>> getAllPhotos() {
        List<Photo> photos = photoRepository.findAllByOrderByCreatedAtDesc();
        return ResponseEntity.ok(photos);
    }
}