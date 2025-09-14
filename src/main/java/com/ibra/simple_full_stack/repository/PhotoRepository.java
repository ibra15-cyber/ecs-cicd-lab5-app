package com.ibra.simple_full_stack.repository;

import com.ibra.simple_full_stack.model.Photo;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface PhotoRepository extends JpaRepository<Photo, Long> {
    List<Photo> findAllByOrderByCreatedAtDesc();
}