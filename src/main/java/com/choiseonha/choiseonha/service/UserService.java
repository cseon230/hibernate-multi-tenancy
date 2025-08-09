package com.choiseonha.choiseonha.service;

import com.choiseonha.choiseonha.entity.User;
import com.choiseonha.choiseonha.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class UserService {

    private final UserRepository userRepository;

    public List<User> getUserList() {
        return userRepository.findAll();
    }
}
