package com.ibra.simple_full_stack.mapper;

import com.ibra.simple_full_stack.dto.PhotoDto;
import com.ibra.simple_full_stack.model.Photo;
import com.ibra.simple_full_stack.util.FileUtils;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;

@Component
public class PhotoMapper {

    public PhotoDto convertToDto(Photo photo) {
        if (photo == null) {
            return null;
        }

        return PhotoDto.builder()
                .id(photo.getId())
                .fileName(photo.getFileName())
                .originalFileName(photo.getOriginalFileName())
                .description(photo.getDescription())
                .presignedUrl(photo.getPresignedUrl())
                .fileSize(photo.getFileSize())
                .contentType(photo.getContentType())
                .createdAt(photo.getCreatedAt())
                .updatedAt(photo.getUpdatedAt())
                .fileSizeFormatted(FileUtils.formatFileSize(photo.getFileSize()))
                .timeAgo(calculateTimeAgo(photo.getCreatedAt()))
                .build();
    }

    private String calculateTimeAgo(LocalDateTime dateTime) {
        if (dateTime == null) return "Unknown";

        LocalDateTime now = LocalDateTime.now();
        long minutes = ChronoUnit.MINUTES.between(dateTime, now);
        long hours = ChronoUnit.HOURS.between(dateTime, now);
        long days = ChronoUnit.DAYS.between(dateTime, now);
        long weeks = days / 7;
        long months = days / 30;
        long years = days / 365;

        if (minutes < 1) return "Just now";
        if (minutes < 60) return minutes + "m ago";
        if (hours < 24) return hours + "h ago";
        if (days < 7) return days + "d ago";
        if (weeks < 4) return weeks + "w ago";
        if (months < 12) return months + "mo ago";
        return years + "y ago";
    }
}