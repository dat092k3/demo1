package com.example.demo.module.catalog.repository;

import com.example.demo.module.catalog.entity.Author;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface AuthorRepository extends JpaRepository<Author, Long> {
    java.util.Optional<Author> findByNameIgnoreCase(String name);
}
