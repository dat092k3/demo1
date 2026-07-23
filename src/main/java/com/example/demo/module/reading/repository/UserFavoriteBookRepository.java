package com.example.demo.module.reading.repository;

import com.example.demo.module.reading.entity.UserFavoriteBook;
import com.example.demo.module.reading.entity.UserFavoriteBookId;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface UserFavoriteBookRepository extends JpaRepository<UserFavoriteBook, UserFavoriteBookId> {
    List<UserFavoriteBook> findByUserId(Long userId);
    Optional<UserFavoriteBook> findByUserIdAndBookId(Long userId, Long bookId);
}
