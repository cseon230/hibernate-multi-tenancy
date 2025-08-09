package com.choiseonha.choiseonha.controller;

import com.choiseonha.choiseonha.entity.User;
import com.choiseonha.choiseonha.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
public class UserController {
    private final UserService userService;

    @GetMapping("/userList")
    public List<User> getUserList(@RequestHeader(value = "X-Tenant-ID", required = false) String tenantId) {
        return userService.getUserList();
    }
}
