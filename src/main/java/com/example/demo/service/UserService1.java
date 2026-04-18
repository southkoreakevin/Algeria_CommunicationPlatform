package com.example.demo.service;

import com.example.demo.domain.User;
import com.example.demo.repository.UserRepository;
import com.example.demo.web.dto.UserJoinRequest;
import com.example.demo.web.dto.UserLoginRequest;
import com.example.demo.web.dto.UserResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class UserService1 implements UserService{

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    @Override
    public void join(UserJoinRequest request){
        if(userRepository.existByEmail(request.email())){
            throw new IllegalStateException("이미 존제하는 이메일 입니다.");
        }

        User user = new User(request.email(), passwordEncoder.encode(request.password()));
        userRepository.save(user);
    }

    @Override
    public boolean login(UserLoginRequest request) {
        return userRepository.findByEmail(request.getUserId())
                .map(user -> passwordEncoder.matches(request.getPassword(), user.getPassword()))
                .orElse(false);
    }

    @Override
    public boolean checkDuplicateEmail(String email) {
        return userRepository.existByEmail(email);
    }

    @Override
    public UserResponse getMyInfo(String userId) {
        return null;
    }
}
