package com.example.demo.module.iam.controller;

import com.example.demo.module.iam.entity.User;
import com.example.demo.module.iam.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;

@RestController
@RequestMapping("/api/user")
public class UserController {

    @Autowired
    private UserRepository userRepository;

    @GetMapping("/authenticate")
    public String hello() {
        return "Hello User";
    }

    @GetMapping("/me")
    public ResponseEntity<?> getCurrentUser(Authentication authentication) {
        String email = authentication.getName();
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("User not found"));
        return ResponseEntity.ok(new UserMeResponse(
                user.getId(),
                user.getName(),
                user.getEmail(),
                user.getPhone(),
                user.getMembershipStatus(),
                user.getRegisteredDate(),
                user.getRoles()
        ));
    }

    record UserMeResponse(
            Long id,
            String name,
            String email,
            String phone,
            String membershipStatus,
            LocalDateTime registeredDate,
            Object roles
    ) {}
}