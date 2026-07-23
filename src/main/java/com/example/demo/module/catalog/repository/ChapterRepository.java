package com.example.demo.module.catalog.repository;

import com.example.demo.module.catalog.entity.Chapter;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ChapterRepository extends JpaRepository<Chapter, Long> {
    List<Chapter> findByBookIdOrderByPageNumberAsc(Long bookId);
}
