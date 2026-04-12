package com.example.demo.service;

import com.example.demo.domain.User;
import com.example.demo.repository.UserRepository;
import com.example.demo.web.dto.UserJoinRequest;
import com.example.demo.web.dto.UserLoginRequest;
import com.example.demo.web.dto.UserResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class UserService1 implements UserService{

    private final UserRepository userRepository;

    public void join(UserJoinRequest request){
        if(userRepository.existByEmail(request.email())){
            throw new IllegalStateException("이미 존제하는 이메일 입니다.");
        }

        User user = new User(request.email(), request.password());
        userRepository.save(user);
    }

    @Override
    public boolean login(UserLoginRequest reqeust) {
        reqeust.getPassword()
    }

    @Override
    public boolean checkDuplicateEmail(String email) {
        return userRepository.existByEmail(email).
    }

    @Override
    public UserResponse getMyInfo(String userId) {
        return null;
    }
}
