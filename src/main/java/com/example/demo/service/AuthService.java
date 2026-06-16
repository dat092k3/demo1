package com.example.demo.service;

import com.example.demo.dto.LoginRequest;
import com.example.demo.dto.RegisterRequest;
import com.example.demo.entity.Role;
import com.example.demo.entity.User;
import com.example.demo.repository.RoleRepository;
import com.example.demo.repository.UserRepository;
import com.example.demo.security.JwtService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class AuthService {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private RoleRepository roleRepository;

    @Autowired
    private JwtService jwtService;

    public void register(RegisterRequest request) {

        if (userRepository.findByEmail(request.getEmail()).isPresent()) {
            throw new RuntimeException("Email already exists");
        }

        Role role = roleRepository
                .findByName("USER")
                .orElseThrow(() ->
                        new RuntimeException("ROLE_USER not found"));

        User user = new User();

        user.setName(request.getName());
        user.setEmail(request.getEmail());
        user.setPassword(request.getPassword());

        user.setPhone(request.getPhone());

        // Nếu client không gửi thì mặc định ACTIVE
        if (request.getMembershipStatus() == null
                || request.getMembershipStatus().isBlank()) {

            user.setMembershipStatus("ACTIVE");

        } else {

            user.setMembershipStatus(
                    request.getMembershipStatus()
            );
        }

        user.setRoles(List.of(role));

        userRepository.save(user);
    }

    public String login(LoginRequest request) {

        User user = userRepository
                .findByEmail(request.getEmail())
                .orElseThrow(() ->
                        new RuntimeException("User not found"));

        if(!user.getPassword()
                .equals(request.getPassword())) {

            throw new RuntimeException(
                    "Wrong password");
        }

        return jwtService.generateToken(
                user.getEmail()
        );
    }
}