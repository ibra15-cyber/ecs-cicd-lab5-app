package com.ibra.simple_full_stack.service.impl;

import com.ibra.simple_full_stack.service.S3Service;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.*;
import software.amazon.awssdk.services.s3.presigner.S3Presigner;
import software.amazon.awssdk.services.s3.presigner.model.GetObjectPresignRequest;
import software.amazon.awssdk.services.s3.presigner.model.PresignedGetObjectRequest;

import java.io.IOException;
import java.time.Duration;
import java.util.UUID;

@Slf4j
@Service
public class S3ServiceImpl implements S3Service {

    private final S3Client s3Client;
    private final S3Presigner s3Presigner;
    
    @Value("${s3.bucket.name}")
    private String bucketName;

    public S3ServiceImpl(S3Client s3Client, S3Presigner s3Presigner) {
        this.s3Client = s3Client;
        this.s3Presigner = s3Presigner;
    }

    @Override
    public boolean uploadFile(MultipartFile file, String fileName) {
        try {
            PutObjectRequest putObjectRequest = PutObjectRequest.builder()
                    .bucket(bucketName)
                    .key(fileName)
                    .contentType(file.getContentType())
                    .contentLength(file.getSize())
                    .metadata(java.util.Map.of(
                        "original-filename", file.getOriginalFilename(),
                        "upload-timestamp", String.valueOf(System.currentTimeMillis())
                    ))
                    .build();

            s3Client.putObject(putObjectRequest, RequestBody.fromBytes(file.getBytes()));
            
            log.info("Successfully uploaded file {} to S3 bucket {}", fileName, bucketName);
            return true;
            
        } catch (IOException e) {
            log.error("Failed to read file content for upload: {}", fileName, e);
            return false;
        } catch (Exception e) {
            log.error("Failed to upload file {} to S3: {}", fileName, e.getMessage(), e);
            return false;
        }
    }

    @Override
    public String generatePresignedUrl(String fileName, int durationInDays) {
        try {
            GetObjectPresignRequest getObjectPresignRequest = GetObjectPresignRequest.builder()
                    .getObjectRequest(GetObjectRequest.builder()
                            .bucket(bucketName)
                            .key(fileName)
                            .build())
                    .signatureDuration(Duration.ofDays(durationInDays))
                    .build();

            PresignedGetObjectRequest presignedGetObjectRequest = s3Presigner.presignGetObject(getObjectPresignRequest);
            String presignedUrl = presignedGetObjectRequest.url().toString();
            
            log.debug("Generated presigned URL for file {} valid for {} days", fileName, durationInDays);
            return presignedUrl;
            
        } catch (Exception e) {
            log.error("Failed to generate presigned URL for file {}: {}", fileName, e.getMessage(), e);
            return null;
        }
    }

    @Override
    public boolean deleteFile(String fileName) {
        try {
            DeleteObjectRequest deleteObjectRequest = DeleteObjectRequest.builder()
                    .bucket(bucketName)
                    .key(fileName)
                    .build();

            s3Client.deleteObject(deleteObjectRequest);
            
            log.info("Successfully deleted file {} from S3 bucket {}", fileName, bucketName);
            return true;
            
        } catch (Exception e) {
            log.error("Failed to delete file {} from S3: {}", fileName, e.getMessage(), e);
            return false;
        }
    }

    @Override
    public boolean doesFileExist(String fileName) {
        try {
            HeadObjectRequest headObjectRequest = HeadObjectRequest.builder()
                    .bucket(bucketName)
                    .key(fileName)
                    .build();

            s3Client.headObject(headObjectRequest);
            return true;
            
        } catch (NoSuchKeyException e) {
            log.debug("File {} does not exist in S3 bucket {}", fileName, bucketName);
            return false;
        } catch (Exception e) {
            log.error("Error checking if file {} exists in S3: {}", fileName, e.getMessage(), e);
            return false;
        }
    }

    @Override
    public String generateUniqueFileName(String originalFilename) {
        if (originalFilename == null || originalFilename.trim().isEmpty()) {
            return UUID.randomUUID().toString();
        }
        
        String extension = "";
        int lastDotIndex = originalFilename.lastIndexOf('.');
        if (lastDotIndex > 0) {
            extension = originalFilename.substring(lastDotIndex);
        }
        
        return UUID.randomUUID().toString() + "_" + originalFilename.replaceAll("[^a-zA-Z0-9._-]", "_");
    }
}