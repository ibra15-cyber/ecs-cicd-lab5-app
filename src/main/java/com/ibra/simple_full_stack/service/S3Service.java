package com.ibra.simple_full_stack.service;

import org.springframework.web.multipart.MultipartFile;

public interface S3Service {
    
    /**
     * Upload a file to S3 bucket
     * @param file The file to upload
     * @param fileName The unique filename to use in S3
     * @return true if upload successful, false otherwise
     */
    boolean uploadFile(MultipartFile file, String fileName);
    
    /**
     * Generate a presigned URL for a file in S3
     * @param fileName The filename in S3
     * @param durationInDays Duration for which the URL should be valid
     * @return Presigned URL string
     */
    String generatePresignedUrl(String fileName, int durationInDays);
    
    /**
     * Delete a file from S3 bucket
     * @param fileName The filename to delete
     * @return true if delete successful, false otherwise
     */
    boolean deleteFile(String fileName);
    
    /**
     * Check if a file exists in S3 bucket
     * @param fileName The filename to check
     * @return true if file exists, false otherwise
     */
    boolean doesFileExist(String fileName);
    
    /**
     * Generate unique filename for uploaded file
     * @param originalFilename Original filename from upload
     * @return Unique filename with UUID prefix
     */
    String generateUniqueFileName(String originalFilename);
}