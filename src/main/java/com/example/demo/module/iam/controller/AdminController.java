package com.example.demo.module.iam.controller;

import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/admin")
public class AdminController {

    @GetMapping("/authenticate")
    public String hello() {
        return "Hello Admin";
    }
}