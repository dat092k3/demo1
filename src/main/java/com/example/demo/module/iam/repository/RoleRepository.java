package com.example.demo.module.iam.repository;

import com.example.demo.module.iam.entity.Role;
import com.example.demo.module.iam.enums.ERole;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface RoleRepository
        extends JpaRepository<Role, Long> {

    Optional<Role> findByName(ERole name);
}