package com.ibra.simple_full_stack.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PhotoDto {
    
    private Long id;
    private String fileName;
    private String description;
    private String presignedUrl;
    
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime createdAt;
    
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime updatedAt;
    
    // File metadata
    private String originalFileName;
    private Long fileSize;
    private String contentType;
    
    // Computed fields
    private String fileSizeFormatted;
    private String timeAgo;
}