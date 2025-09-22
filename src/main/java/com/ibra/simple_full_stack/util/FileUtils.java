package com.ibra.simple_full_stack.util;

import java.text.DecimalFormat;

public class FileUtils {

    private static final DecimalFormat DECIMAL_FORMAT = new DecimalFormat("#.##");
    private static final String[] SIZE_UNITS = {"B", "KB", "MB", "GB", "TB"};

    /**
     * Format file size in bytes to human-readable format
     * @param bytes File size in bytes
     * @return Formatted string (e.g., "2.5 MB")
     */
    public static String formatFileSize(Long bytes) {
        if (bytes == null || bytes <= 0) {
            return "0 B";
        }

        double size = bytes.doubleValue();
        int unitIndex = 0;

        while (size >= 1024 && unitIndex < SIZE_UNITS.length - 1) {
            size /= 1024;
            unitIndex++;
        }

        return DECIMAL_FORMAT.format(size) + " " + SIZE_UNITS[unitIndex];
    }

    /**
     * Validate if the content type is an allowed image type
     * @param contentType MIME type to validate
     * @return true if valid image type, false otherwise
     */
    public static boolean isValidImageType(String contentType) {
        if (contentType == null) {
            return false;
        }
        
        return contentType.toLowerCase().matches("image/(jpeg|jpg|png|gif|webp)");
    }

    /**
     * Get file extension from filename
     * @param filename Original filename
     * @return File extension including the dot (e.g., ".jpg") or empty string if no extension
     */
    public static String getFileExtension(String filename) {
        if (filename == null || filename.trim().isEmpty()) {
            return "";
        }
        
        int lastDotIndex = filename.lastIndexOf('.');
        if (lastDotIndex > 0 && lastDotIndex < filename.length() - 1) {
            return filename.substring(lastDotIndex);
        }
        
        return "";
    }

    /**
     * Sanitize filename by removing or replacing invalid characters
     * @param filename Original filename
     * @return Sanitized filename safe for storage
     */
    public static String sanitizeFilename(String filename) {
        if (filename == null || filename.trim().isEmpty()) {
            return "unnamed_file";
        }
        
        return filename.trim()
                .replaceAll("[^a-zA-Z0-9._-]", "_")
                .replaceAll("_{2,}", "_"); // Replace multiple underscores with single
    }
}