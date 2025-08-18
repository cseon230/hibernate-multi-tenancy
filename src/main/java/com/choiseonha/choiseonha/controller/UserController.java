package com.choiseonha.choiseonha.controller;

import com.choiseonha.choiseonha.entity.User;
import com.choiseonha.choiseonha.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
public class UserController {
    private final UserService userService;

    @GetMapping("/userlist")
    public List<User> getUserList(@RequestHeader(value = "X-Tenant-ID", required = false) String tenantId) {
        return userService.getUserList();
    }

    @PutMapping("/userpassword")
    public User updateUserPwd(@RequestHeader(value = "X-Tenant-ID", required = false) String tenantId,
                              @RequestParam(name = "userNo") Integer userNo,
                              @RequestParam(name = "userPwd") String userPwd) {
        return userService.updateUserPwd(userNo, userPwd);
    }
}
