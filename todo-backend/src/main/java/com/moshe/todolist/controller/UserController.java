package com.moshe.todolist.controller;

import com.moshe.todolist.entity.User;
import com.moshe.todolist.service.UserService;
import lombok.Data;
import org.springframework.web.bind.annotation.*;

@RestController
@CrossOrigin  // 允许前端跨域访问
@RequestMapping("/api/users")
public class UserController {

    private final UserService userService;

    public UserController(UserService userService) {
        this.userService = userService;
    }

    // === 1. 注册接口 ===
    // POST /api/users/register
    @PostMapping("/register")
    public User register(@RequestBody UserRequest req) {
        return userService.register(req.getName(), req.getPassword());
    }

    // === 2. 登录接口 ===
    // POST /api/users/login
    @PostMapping("/login")
    public User login(@RequestBody UserRequest req) {
        return userService.login(req.getName(), req.getPassword());
    }

    // 简单的请求体 DTO
    @Data
    public static class UserRequest {
        private String name;
        private String password;
    }
}