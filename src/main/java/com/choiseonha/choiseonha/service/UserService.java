package com.choiseonha.choiseonha.service;

import com.choiseonha.choiseonha.entity.User;
import com.choiseonha.choiseonha.repository.UserRepository;
import jakarta.persistence.EntityNotFoundException;
import jakarta.transaction.Transactional;
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

    @Transactional
    public User updateUserPwd(Integer userNo, String userPwd) {
        User user = userRepository.findById(userNo)
                        .orElseThrow(() -> new EntityNotFoundException("해당 유저를 찾을 수 없습니다. userNo="+ userNo));
        user.setUserPwd(userPwd);
        return userRepository.save(user);
    }
}
