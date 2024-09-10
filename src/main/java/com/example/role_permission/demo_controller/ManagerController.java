package com.example.role_permission.demo_controller;

import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/manager")
public class ManagerController {

    @GetMapping
    public String get() {
        return "GET:manager";
    }
    @PostMapping
    public String post() {
        return "POST:manager";
    }
    @PutMapping
    public String update() {
        return "PUT:manager";
    }
    @DeleteMapping
    public String delete() {
        return "DELETE:manager";
    }
}

