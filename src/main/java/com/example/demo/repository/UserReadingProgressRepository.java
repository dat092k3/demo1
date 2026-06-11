package com.example.demo.repository;

import com.example.demo.entity.UserReadingProgress;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

import com.example.demo.entity.UserReadingProgressId;

@Repository
public interface UserReadingProgressRepository extends JpaRepository<UserReadingProgress, UserReadingProgressId> {
    Optional<UserReadingProgress> findByUserIdAndBookId(Long userId, Long bookId);
}
